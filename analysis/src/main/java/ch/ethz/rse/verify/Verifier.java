package ch.ethz.rse.verify;

import java.util.Collection;
import java.util.LinkedList;
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
import apron.Tcons1;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.numerical.NumericalAnalysis;
import ch.ethz.rse.numerical.NumericalStateWrapper;
import ch.ethz.rse.pointer.EventInitializer;
import ch.ethz.rse.pointer.PointsToInitializer;
import ch.ethz.rse.utils.Constants;
import polyglot.ast.Call;
import soot.Local;
import soot.SootClass;
import soot.SootHelper;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.UnitGraph;

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
    for (SootMethod method : this.numericalAnalysis.keySet()) {
      NumericalAnalysis analysis = this.numericalAnalysis.get(method);
      Manager man = analysis.man;

      Collection<EventInitializer> events = this.pointsTo.getInitializers(method);
      // Multimap<EventInitializer, JInvokeStmt> eventInvoke = analysis.eventInvoke;
      Map<JInvokeStmt, Abstract1> invokeToAbstract = analysis.invokeToAbstract;

      for (EventInitializer event : events) {
        Collection<JInvokeStmt> invokes = event.getInvokes();

        for (JInvokeStmt invokeStmt : invokes) {
          InvokeExpr expr = invokeStmt.getInvokeExpr();
          // only analyze constructor invokes
          if (expr instanceof SpecialInvokeExpr) {
            logger.debug("Checking event " + event.toString() + " with invoke statement " + invokeStmt);
            Value start = expr.getArg(0);
            Value end = expr.getArg(1);

            Lincons1 lincons1 = analysis.getConstraint(start, end, Lincons1.SUP);
            Abstract1 fallout = invokeToAbstract.get(invokeStmt);
            logger.debug("Fallout: " + fallout);
            // The following fails because for some reason there is no mapping for the
            // statement and therefore fallout is null

            try {
              fallout.meet(man, lincons1);
              if (!fallout.isBottom(man))
                // start - end > 0 => property not satisfied
                return false;
            } catch (ApronException e1) {
              e1.printStackTrace();
            }
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean checkAfterStart() {
    // TODO: FILL THIS OUT
    return true;
  }

  @Override
  public boolean checkBeforeEnd() {
    // TODO: FILL THIS OUT
    return true;
  }

  // TODO: MAYBE FILL THIS OUT: add convenience methods

}
