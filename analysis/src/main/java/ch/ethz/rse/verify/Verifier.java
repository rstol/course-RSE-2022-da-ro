package ch.ethz.rse.verify;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Lincons1;
import apron.Linexpr1;
import apron.Linterm1;
import apron.Manager;
import apron.MpqScalar;
import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.numerical.NumericalAnalysis;
import ch.ethz.rse.pointer.EventInitializer;
import ch.ethz.rse.pointer.PointsToInitializer;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JInvokeStmt;

/**
 * Main class handling verification
 *
 */
public class Verifier extends AVerifier {

  private static final Logger logger = LoggerFactory.getLogger(Verifier.class);

  /**
   * class to be verified
   */
  private final SootClass c;

  /**
   * points to analysis for verified class
   */
  private final PointsToInitializer pointsTo;

  /**
   *
   * @param c class to verify
   */
  public Verifier(SootClass c) {
    logger.debug("Analyzing {}", c.getName());

    this.c = c;

    // pointer analysis
    this.pointsTo = new PointsToInitializer(this.c);
  }

  protected void runNumericalAnalysis(VerificationProperty property) {
    for (SootMethod method : this.c.getMethods()) {
      NumericalAnalysis analysis = new NumericalAnalysis(method, property, this.pointsTo);
      this.numericalAnalysis.put(method, analysis);
    }
  }

  @Override
  public boolean checkStartEndOrder() {
    // return true if no methods in test
    if (numericalAnalysis.isEmpty()) {
      return true;
    }

    boolean startEndOrder = true;

    for (SootMethod method : this.numericalAnalysis.keySet()) {
      NumericalAnalysis analysis = this.numericalAnalysis.get(method);
      Manager man = analysis.man;
      Environment env = analysis.env;
      Collection<EventInitializer> events = this.pointsTo.getInitializers(method);
      Map<JInvokeStmt, Abstract1> invokeToAbstract = analysis.invokeToAbstract;

      for (EventInitializer event : events) {
        int start = event.start;
        for (JInvokeStmt invokeStmt : event.getInvokes()) {
          InvokeExpr expr = invokeStmt.getInvokeExpr();
          // only analyze constructor invokes
          if (expr instanceof SpecialInvokeExpr) {
            logger.debug("Checking event " + event.toString() + " with invoke statement " + invokeStmt);
            Value end = expr.getArg(1);
            if (end instanceof Local) {
              Abstract1 fallout = invokeToAbstract.get(invokeStmt);
              Linterm1 linterm1 = new Linterm1(((Local) end).getName(), new MpqScalar(-1));
              Linexpr1 linexpr1 = new Linexpr1(env, new Linterm1[] { linterm1 }, new MpqScalar(start));
              Lincons1 lincons1 = new Lincons1(Lincons1.SUP, linexpr1);
              logger.debug("Constraint: " + lincons1);
              try {
                fallout.meet(man, lincons1);
                for (Abstract1 f : invokeToAbstract.values()) {
                  logger.debug("Fallout is " + f);
                }
                for (Abstract1 f : invokeToAbstract.values()) {
                  logger.debug("Fallout is " + f);
                }
                logger.debug("fallout: " + fallout);
                if (!fallout.isBottom(man))
                  // start - end > 0 => property might not be satisfied
                  startEndOrder = false;
              } catch (ApronException e1) {
                e1.printStackTrace();
              }
            } else if (end instanceof IntConstant) {
              if (start > ((IntConstant) end).value) {
                startEndOrder = false;
              }
            }
          }
        }
      }
    }
    return startEndOrder;
  }

  @Override
  public boolean checkAfterStart() {
    // return true if no methods in test
    if (numericalAnalysis.isEmpty()) {
      return true;
    }

    boolean afterStart = true;

    for (SootMethod method : this.numericalAnalysis.keySet()) {
      NumericalAnalysis analysis = this.numericalAnalysis.get(method);
      Manager man = analysis.man;
      Environment env = analysis.env;
      Collection<EventInitializer> events = this.pointsTo.getInitializers(method);
      Map<JInvokeStmt, Abstract1> invokeToAbstract = analysis.invokeToAbstract;

      for (EventInitializer event : events) {
        int start = event.start;
        for (JInvokeStmt invokeStmt : event.getInvokes()) {
          InvokeExpr expr = invokeStmt.getInvokeExpr();
          // only analyze switchlights invokes
          if (expr instanceof VirtualInvokeExpr) {
            logger.debug("Checking event " + event.toString() + " with invoke statement " + invokeStmt);
            Value time = expr.getArg(0);
            if (time instanceof Local) {
              Abstract1 fallout = invokeToAbstract.get(invokeStmt);
              Linterm1 linterm1 = new Linterm1(((Local) time).getName(), new MpqScalar(-1));
              Linexpr1 linexpr1 = new Linexpr1(env, new Linterm1[] { linterm1 }, new MpqScalar(start));
              Lincons1 lincons1 = new Lincons1(Lincons1.SUP, linexpr1);
              logger.debug("Constraint: " + lincons1);
              try {
                fallout.meet(man, lincons1);
                // for (Abstract1 f : invokeToAbstract.values()) {
                // logger.debug("Fallout is " + f);
                // }
                logger.debug("fallout: " + fallout);
                if (!fallout.isBottom(man))
                  // time - start > 0 => property might not be satisfied
                  afterStart = false;
              } catch (ApronException e1) {
                e1.printStackTrace();
              }
            } else if (time instanceof IntConstant) {
              if (((IntConstant) time).value < start) {
                afterStart = false;
              }
            }
          }
        }
      }
    }
    return afterStart;
  }

  @Override
  public boolean checkBeforeEnd() {
    // return true if no methods in test
    if (numericalAnalysis.isEmpty()) {
      return true;
    }

    boolean beforeEnd = true;

    for (SootMethod method : this.numericalAnalysis.keySet()) {
      NumericalAnalysis analysis = this.numericalAnalysis.get(method);
      Manager man = analysis.man;
      Environment env = analysis.env;
      Collection<EventInitializer> events = this.pointsTo.getInitializers(method);
      Map<JInvokeStmt, Abstract1> invokeToAbstract = analysis.invokeToAbstract;

      for (EventInitializer event : events) {
        Value end = event.getStatement().getInvokeExpr().getArg(1);
        logger.debug("End is " + end.toString());
        for (JInvokeStmt invokeStmt : event.getInvokes()) {
          InvokeExpr expr = invokeStmt.getInvokeExpr();
          // only analyze switchlights invokes
          if (expr instanceof VirtualInvokeExpr) {
            logger.debug("Checking event " + event.toString() + " with invoke statement " + invokeStmt);
            Value time = expr.getArg(0);
            if (time instanceof Local) {
              Abstract1 fallout = invokeToAbstract.get(invokeStmt);
              Linterm1 ltTime = new Linterm1(((Local) time).getName(), new MpqScalar(1));
              Linexpr1 linexpr1 = null;
              if (end instanceof Local) {
                Linterm1 ltEnd = new Linterm1(((Local) end).getName(), new MpqScalar(-1));
                linexpr1 = new Linexpr1(env, new Linterm1[] { ltTime, ltEnd }, new MpqScalar(0));
              } else if (end instanceof IntConstant) {
                linexpr1 = new Linexpr1(env, new Linterm1[] { ltTime }, new MpqScalar(-((IntConstant) end).value));
              }
              Lincons1 lincons1 = new Lincons1(Lincons1.SUP, linexpr1);
              logger.debug("Constraint: " + lincons1);
              try {
                fallout.meet(man, lincons1);
                logger.debug("fallout: " + fallout);
                if (!fallout.isBottom(man))
                  // time - end > 0 => property might not be satisfied
                  beforeEnd = false;
              } catch (ApronException e1) {
                e1.printStackTrace();
              }
            } else if (time instanceof IntConstant) {
              int t = ((IntConstant) time).value;
              if (end instanceof Local) {
                Linterm1 ltEnd = new Linterm1(((Local) end).getName(), new MpqScalar(-1));
                Linexpr1 linexpr1 = new Linexpr1(env, new Linterm1[] { ltEnd }, new MpqScalar(t));
                Lincons1 lincons1 = new Lincons1(Lincons1.SUP, linexpr1);
                logger.debug("Constraint: " + lincons1);
                Abstract1 fallout = invokeToAbstract.get(invokeStmt);
                try {
                  fallout.meet(man, lincons1);
                  logger.debug("fallout: " + fallout);
                  if (!fallout.isBottom(man))
                    // time - end > 0 => property might not be satisfied
                    beforeEnd = false;
                } catch (ApronException e1) {
                  e1.printStackTrace();
                }
              } else if (end instanceof IntConstant) {
                if (t > ((IntConstant) end).value) {
                  beforeEnd = false;
                }
              }
            }
          }
        }
      }
    }
    return beforeEnd;
  }

  // TODO: MAYBE FILL THIS OUT: add convenience methods

}
