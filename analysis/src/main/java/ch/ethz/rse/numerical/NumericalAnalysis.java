package ch.ethz.rse.numerical;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Interval;
import apron.Lincons1;
import apron.Linexpr1;
import apron.Linterm1;
import apron.Manager;
import apron.MpqScalar;
import apron.Polka;
import apron.StringVar;
import apron.Tcons1;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.pointer.EventInitializer;
import ch.ethz.rse.pointer.PointsToInitializer;
import ch.ethz.rse.utils.Constants;
import ch.ethz.rse.verify.EnvironmentGenerator;
import soot.ArrayType;
import soot.DoubleType;
import soot.Local;
import soot.RefType;
import soot.SootHelper;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.MulExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.SubExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JSubExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;

/**
 * Convenience class running a numerical analysis on a given {@link SootMethod}
 */
public class NumericalAnalysis extends ForwardBranchedFlowAnalysis<NumericalStateWrapper> {

  private static final Logger logger = LoggerFactory.getLogger(NumericalAnalysis.class);

  /**
   * the property we are verifying
   */
  private final VerificationProperty property;

  /**
   * the pointer analysis result we are verifying
   */
  private final PointsToInitializer pointsTo;

  /**
   * all event initializers encountered until now
   */
  private Set<EventInitializer> alreadyInit;

  /**
   * number of times this loop head was encountered during analysis
   */
  private HashMap<Unit, IntegerWrapper> loopHeads = new HashMap<Unit, IntegerWrapper>();
  /**
   * Previously seen abstract state for each loop head
   */
  private HashMap<Unit, NumericalStateWrapper> loopHeadState = new HashMap<Unit, NumericalStateWrapper>();

  /**
   * Numerical abstract domain to use for analysis: Convex polyhedra
   */
  public final Manager man = new Polka(true);

  public final Environment env;

  /**
   * We apply widening after updating the state at a given merge point for the
   * {@link WIDENING_THRESHOLD}th time
   */
  private static final int WIDENING_THRESHOLD = 6;

  // Map invoke statements to their corresponding abstact value. Used for
  // verification
  public Map<JInvokeStmt, Abstract1> invokeToAbstract = new HashMap<JInvokeStmt, Abstract1>();

  /**
   *
   * @param method   method to analyze
   * @param property the property we are verifying
   */
  public NumericalAnalysis(SootMethod method, VerificationProperty property, PointsToInitializer pointsTo) {
    super(SootHelper.getUnitGraph(method));

    UnitGraph g = SootHelper.getUnitGraph(method);

    this.property = property;

    this.pointsTo = pointsTo;

    this.alreadyInit = new HashSet<EventInitializer>();

    this.env = new EnvironmentGenerator(method, pointsTo).getEnvironment();

    // initialize counts for loop heads
    for (Loop l : new LoopNestTree(g.getBody())) {
      loopHeads.put(l.getHead(), new IntegerWrapper(0));
    }

    // perform analysis by calling into super-class
    logger.debug("Analyzing {} in {}", method.getName(), method.getDeclaringClass().getName());
    doAnalysis(); // calls newInitialFlow, entryInitialFlow, merge, flowThrough, and stops when a
                  // fixed point is reached
  }

  /**
   * Report unhandled instructions, types, cases, etc.
   *
   * @param task description of current task
   * @param what
   */
  public static void unhandled(String task, Object what, boolean raiseException) {
    String description = task + ": Can't handle " + what.toString() + " of type " + what.getClass().getName();

    if (raiseException) {
      logger.error("Raising exception " + description);
      throw new UnsupportedOperationException(description);
    } else {
      logger.error(description);

      // print stack trace
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      for (int i = 1; i < stackTrace.length; i++) {
        logger.error(stackTrace[i].toString());
      }
    }
  }

  @Override
  protected void copy(NumericalStateWrapper source, NumericalStateWrapper dest) {
    source.copyInto(dest);
  }

  @Override
  protected NumericalStateWrapper newInitialFlow() {
    // should be bottom (only entry flows are not bottom originally)
    return NumericalStateWrapper.bottom(man, env);
  }

  @Override
  protected NumericalStateWrapper entryInitialFlow() {
    // state of entry points into function
    NumericalStateWrapper ret = NumericalStateWrapper.top(man, env);

    // TODO: MAYBE FILL THIS OUT

    return ret;
  }

  @Override
  protected void merge(Unit succNode, NumericalStateWrapper w1, NumericalStateWrapper w2, NumericalStateWrapper w3) {
    // merge the two states from w1 and w2 and store the result into w3
    logger.debug("in merge: " + succNode);
    IntegerWrapper headCount = loopHeads.get(succNode);

    if (headCount != null) {
      // perform widening or join
      if (headCount.value < WIDENING_THRESHOLD) {
        w3.set(w1.join(w2).get());
      } else {
        w3.set(w1.widen(w2).get());
      }
      headCount.value++;
    } else {
      w3.set(w1.join(w2).get());
    }
  }

  @Override
  protected void merge(NumericalStateWrapper src1, NumericalStateWrapper src2, NumericalStateWrapper trg) {
    // this method is never called, we are using the other merge instead
    throw new UnsupportedOperationException();
  }

  @Override
  protected void flowThrough(NumericalStateWrapper inWrapper, Unit op, List<NumericalStateWrapper> fallOutWrappers,
      List<NumericalStateWrapper> branchOutWrappers) {
    logger.debug(inWrapper + " " + op + " => ?");

    Stmt s = (Stmt) op;

    // fallOutWrapper is the wrapper for the state after running op,
    // assuming we move to the next statement. Do not overwrite
    // fallOutWrapper, but use its .set method instead
    assert fallOutWrappers.size() <= 1;
    NumericalStateWrapper fallOutWrapper = null;
    if (fallOutWrappers.size() == 1) {
      fallOutWrapper = fallOutWrappers.get(0);
      inWrapper.copyInto(fallOutWrapper);
    }

    // branchOutWrapper is the wrapper for the state after running op,
    // assuming we follow a conditional jump. It is therefore only relevant
    // if op is a conditional jump. In this case, (i) fallOutWrapper
    // contains the state after "falling out" of the statement, i.e., if the
    // condition is false, and (ii) branchOutWrapper contains the state
    // after "branching out" of the statement, i.e., if the condition is
    // true.
    assert branchOutWrappers.size() <= 1;
    NumericalStateWrapper branchOutWrapper = null;
    if (branchOutWrappers.size() == 1) {
      branchOutWrapper = branchOutWrappers.get(0);
      inWrapper.copyInto(branchOutWrapper);
    }

    try {
      if (s instanceof DefinitionStmt) {
        // handle assignment

        DefinitionStmt sd = (DefinitionStmt) s;
        Value left = sd.getLeftOp();
        Value right = sd.getRightOp();

        // We are not handling these cases:
        if (!(left instanceof JimpleLocal)) {
          unhandled("Assignment to non-local variable", left, true);
        } else if (left instanceof JArrayRef) {
          unhandled("Assignment to a non-local array variable", left, true);
        } else if (left.getType() instanceof ArrayType) {
          unhandled("Assignment to Array", left, true);
        } else if (left.getType() instanceof DoubleType) {
          unhandled("Assignment to double", left, true);
        } else if (left instanceof JInstanceFieldRef) {
          unhandled("Assignment to field", left, true);
        }

        if (left.getType() instanceof RefType) {
          // assignments to references are handled by pointer analysis
          // no action necessary
        } else {
          // handle assignment
          handleDef(fallOutWrapper, left, right);
        }

      } else if (s instanceof JIfStmt) {
        // handle if
        JIfStmt ifStmt = (JIfStmt) s;
        Value cond = ifStmt.getCondition();
        logger.debug("Condition type of if statement is: " + cond.getType());
        AbstractBinopExpr condExpr = (AbstractBinopExpr) cond;
        handleIf(condExpr, inWrapper, fallOutWrapper, branchOutWrapper);

      } else if (s instanceof JInvokeStmt) {
        // handle invocations
        JInvokeStmt jInvStmt = (JInvokeStmt) s;
        InvokeExpr invokeExpr = jInvStmt.getInvokeExpr();
        if (invokeExpr instanceof JVirtualInvokeExpr) {
          handleInvoke(jInvStmt, fallOutWrapper);
        } else if (invokeExpr instanceof JSpecialInvokeExpr) {
          // initializer for object
          handleInitialize(jInvStmt, fallOutWrapper);
        } else {
          unhandled("Unhandled invoke statement", invokeExpr, true);
        }
      } else if (s instanceof JGotoStmt) {
        // safe to ignore
      } else if (s instanceof JReturnVoidStmt) {
        // safe to ignore
      } else {
        unhandled("Unhandled statement", s, true);
      }

      // log outcome
      if (fallOutWrapper != null) {
        logger.debug(inWrapper.get() + " " + s + " =>[fallout] " + fallOutWrapper);
      }
      if (branchOutWrapper != null) {
        logger.debug(inWrapper.get() + " " + s + " =>[branchout] " + branchOutWrapper);
      }

    } catch (ApronException e) {
      throw new RuntimeException(e);
    }
  }

  public void handleInvoke(JInvokeStmt jInvStmtInit, NumericalStateWrapper fallOutWrapper) throws ApronException {
    Abstract1 fallout = fallOutWrapper.get();
    JInvokeStmt jInvStmt = (JInvokeStmt) jInvStmtInit.clone();
    VirtualInvokeExpr expr = (VirtualInvokeExpr) jInvStmt.getInvokeExpr();
    if (expr.getMethod().getName().equals(Constants.switchLightsFunctionName)) {
      Local base = (Local) expr.getBase();
      List<EventInitializer> events = pointsTo.pointsTo(base);
      Value arg0 = expr.getArg(0); // switchLights only has one argument
      Interval intervalArg = getInterval(arg0, fallout);

      for (EventInitializer event : events) {
        event.addInvoke(jInvStmt);

        Interval intervalEvent = getInterval(event.getVar(), fallout);
        Interval newIntervalCombined = combineIntervals(intervalArg, intervalEvent);
        logger.debug("Event interval: " + intervalEvent);
        logger.debug("Arg interval : " + intervalArg);
        logger.debug("Combined interval: " + newIntervalCombined);
        Lincons1 lincons1 = new Lincons1(Lincons1.EQ, new Linexpr1(env,
            new Linterm1[] { new Linterm1(event.getVar(), new MpqScalar(-1)) },
            newIntervalCombined));
        fallout.forget(man, event.getVar(), false);
        fallout.meet(man, lincons1);
        fallOutWrapper.set(fallout);
      }
      invokeToAbstract.put(jInvStmt, fallOutWrapper.copy().get());
    }
  }

  public void handleInitialize(JInvokeStmt jInvStmtInit, NumericalStateWrapper fallOutWrapper) throws ApronException {
    Abstract1 fallout = fallOutWrapper.get();
    JInvokeStmt jInvStmt = (JInvokeStmt) jInvStmtInit.clone();
    JSpecialInvokeExpr expr = (JSpecialInvokeExpr) jInvStmt.getInvokeExpr();
    Local base = (Local) expr.getBase();
    List<EventInitializer> events = this.pointsTo.pointsTo(base);

    // only handle initializer of our Event class
    if (expr.getMethod().getDeclaringClass().getName().equals(Constants.EventClassName)) {
      logger.debug("Initializer: " + expr.getMethod() + " args: " + expr.getArg(0) + ", " + expr.getArg(1));
      Interval intervalStart = getInterval(expr.getArg(0), fallout);
      Interval intervalEnd = getInterval(expr.getArg(1), fallout);
      logger.debug("Start interval: " + intervalStart);
      logger.debug("End interval: " + intervalEnd);
      Interval eventIntervalCombined = combineIntervals(intervalStart, intervalEnd);
      for (EventInitializer event : events) {
        if (!alreadyInit.contains(event)) {
          alreadyInit.add(event);
          event.addInvoke(jInvStmt);

          Interval intervalEvent = getInterval(event.getVar(), fallout);
          Interval newIntervalCombined = combineIntervals(eventIntervalCombined, intervalEvent);
          logger.debug("Event new interval: " + eventIntervalCombined);
          logger.debug("Event old interval: " + intervalEvent);
          logger.debug("Combined interval: " + newIntervalCombined);

          Lincons1 lincons1 = new Lincons1(Lincons1.EQ, new Linexpr1(env,
              new Linterm1[] { new Linterm1(event.getVar(), new MpqScalar(-1)) },
              newIntervalCombined));
          fallout.forget(man, event.getVar(), false);
          fallout.meet(man, lincons1);
          fallOutWrapper.set(fallout);
        }
      }
      invokeToAbstract.put(jInvStmt, fallOutWrapper.copy().get());
      logger.debug("adding to invoke abstract map " + jInvStmt + " and " + fallOutWrapper.get());
    }
  }

  /**
   * TODO: This method needs testing
   * Returns state of in after assignment
   *
   * @param outWrapper
   * @param left
   * @param right
   * @throws ApronException
   */
  private void handleDef(NumericalStateWrapper outWrapper, Value left, Value right) throws ApronException {
    Abstract1 inState = outWrapper.get();
    if (left instanceof JimpleLocal) {
      String leftLocal = ((JimpleLocal) left).getName();
      Texpr1Node rightExpr = null;
      Texpr1Intern expr = null;
      if (right instanceof IntConstant) {
        rightExpr = new Texpr1CstNode(new MpqScalar(((IntConstant) right).value));
        expr = new Texpr1Intern(env, rightExpr);
        // maybe better to use .set than assign
        inState.assign(man, leftLocal, expr, null);
      } else if (right instanceof JimpleLocal) {
        JimpleLocal rightLocal = (JimpleLocal) right;
        if (SootHelper.isIntValue(rightLocal)) {
          rightExpr = new Texpr1VarNode(rightLocal.getName());
          expr = new Texpr1Intern(env, rightExpr);
          inState.assign(man, leftLocal, expr, null);
        }
      } else if (right instanceof BinopExpr) {
        // TODO: use Linterm in this implementation
        if (right instanceof JMulExpr) {
          handleBinExpr(inState, (JMulExpr) right, Texpr1BinNode.OP_MUL, leftLocal);
        } else if (right instanceof JSubExpr) {
          handleBinExpr(inState, (JSubExpr) right, Texpr1BinNode.OP_SUB, leftLocal);
        } else if (right instanceof JAddExpr) {
          handleBinExpr(inState, (JSubExpr) right, Texpr1BinNode.OP_ADD, leftLocal);
        }
      }
    }
  }

  /**
   * TODO: this method needs testing
   *
   * @param condExpr
   * @param inWrapper
   * @param fallOutWrapper
   * @param branchOutWrapper
   * @throws ApronException
   */
  private void handleIf(AbstractBinopExpr condExpr, NumericalStateWrapper inWrapper,
      NumericalStateWrapper fallOutWrapper, NumericalStateWrapper branchOutWrapper) throws ApronException {
    Value left = condExpr.getOp1();
    Value right = condExpr.getOp2();
    Abstract1 inState = inWrapper.get();

    Lincons1 fallOutConstraint = null;
    Lincons1 branchOutConstraint = null;

    if (condExpr instanceof JEqExpr) {
      fallOutConstraint = getConstraint(left, right, Lincons1.DISEQ);
      branchOutConstraint = getConstraint(left, right, Lincons1.EQ);
    } else if (condExpr instanceof JGeExpr) {
      fallOutConstraint = getConstraint(right, left, Lincons1.SUP);
      branchOutConstraint = getConstraint(left, right, Lincons1.SUPEQ);
    } else if (condExpr instanceof JGtExpr) {
      fallOutConstraint = getConstraint(right, left, Lincons1.SUPEQ);
      branchOutConstraint = getConstraint(left, right, Lincons1.SUP);
    } else if (condExpr instanceof JLeExpr) {
      fallOutConstraint = getConstraint(left, right, Lincons1.SUP);
      branchOutConstraint = getConstraint(right, left, Lincons1.SUPEQ);
    } else if (condExpr instanceof JLtExpr) {
      fallOutConstraint = getConstraint(left, right, Lincons1.SUPEQ);
      branchOutConstraint = getConstraint(right, left, Lincons1.SUP);
    } else if (condExpr instanceof JNeExpr) {
      fallOutConstraint = getConstraint(left, right, Lincons1.EQ);
      branchOutConstraint = getConstraint(left, right, Lincons1.DISEQ);
    } else {
      unhandled("Unhandled conditional statement", condExpr, true);
    }

    fallOutWrapper.set(inState.meetCopy(man, fallOutConstraint));
    branchOutWrapper.set(inState.meetCopy(man, branchOutConstraint));
  }

  // TODO: Remove this method
  public Texpr1Node makeExprFromValue(Value v) {
    Texpr1Node e = null;
    if (v instanceof JimpleLocal) {
      if (SootHelper.isIntValue(v)) {
        e = new Texpr1VarNode(((JimpleLocal) v).getName());
      }
    } else if (v instanceof IntConstant) {
      e = new Texpr1CstNode(new MpqScalar(((IntConstant) v).value));
    } else {
      throw new UnsupportedOperationException("Can't handle this type of argument");
    }
    return e;
  }

  private void setLinexprWithValue(Value v, Linexpr1 linexpr1, int sign) {
    if (v instanceof JimpleLocal) {
      if (SootHelper.isIntValue(v)) {
        linexpr1.setCoeff(new StringVar(((JimpleLocal) v).getName()), new MpqScalar(sign));
      }
    } else if (v instanceof IntConstant) {
      linexpr1.setCst(new MpqScalar(sign * ((IntConstant) v).value));
    } else {
      throw new UnsupportedOperationException("Can't handle this type of argument");
    }
  }

  public Lincons1 getConstraint(Value op1, Value op2, int binOp) {
    Linexpr1 linexpr1 = new Linexpr1(env);
    setLinexprWithValue(op1, linexpr1, 1);
    setLinexprWithValue(op2, linexpr1, -1);
    return new Lincons1(binOp, linexpr1);
  }

  public Interval getInterval(Object obj, Abstract1 elem) throws ApronException {
    if (obj instanceof Local) {
      return elem.getBound(man, ((Local) obj).getName());
    } else if (obj instanceof ParameterRef) {
      // Parameters are unknown and can thus TOP
      Interval interval = new Interval();
      interval.setTop();
      return interval;
    } else if (obj instanceof IntConstant) {
      // For convenience, integers are point intervals.
      MpqScalar value = new MpqScalar(((IntConstant) obj).value);
      return new Interval(value, value);
    } else if (obj instanceof String) {
      return elem.getBound(man, (String) obj);
    } else {
      return null;
    }
  }

  public Interval combineIntervals(Interval i1, Interval i2) {
    Interval intervalCombined = new Interval();
    if (i1.inf.cmp(i2.inf) == 1) {
      intervalCombined.setInf(i2.inf);
    } else {
      intervalCombined.setInf(i1.inf);
    }
    if (i1.sup.cmp(i2.sup) == 1) {
      intervalCombined.setSup(i1.sup);
    } else {
      intervalCombined.setSup(i2.sup);
    }
    return intervalCombined;
  }

  // TODO: Remove this method
  private void handleBinExpr(Abstract1 inState, AbstractBinopExpr right, int op, String var) {
    Value op1 = right.getOp1();
    Value op2 = right.getOp2();
    Texpr1Node leftExpr = makeExprFromValue(op1);
    Texpr1Node rightExpr = makeExprFromValue(op2);
    Texpr1BinNode bin = new Texpr1BinNode(Texpr1BinNode.OP_MUL, leftExpr, rightExpr);
    Texpr1Intern expr = new Texpr1Intern(env, bin);
    try {
      inState.assign(man, var, expr, null);
    } catch (ApronException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
