package ch.ethz.rse.verify;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.Abstract1;
import apron.ApronException;
import apron.Lincons1;
import apron.Manager;
import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.numerical.NumericalAnalysis;
import ch.ethz.rse.pointer.EventInitializer;
import ch.ethz.rse.pointer.PointsToInitializer;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
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
    if (numericalAnalysis.isEmpty()) {
      return true;
    }

    boolean startEndOrder = true;

    for (SootMethod method : this.numericalAnalysis.keySet()) {
      NumericalAnalysis analysis = this.numericalAnalysis.get(method);
      Manager man = analysis.man;
      Collection<EventInitializer> events = this.pointsTo.getInitializers(method);
      Map<JInvokeStmt, Abstract1> invokeToAbstract = analysis.invokeToAbstract;

      for (EventInitializer event : events) {
        Value start = event.getStatement().getInvokeExpr().getArg(0);
        logger.debug("Checking event " + event);
        for (JInvokeStmt invokeStmt : event.getInvokes()) {
          InvokeExpr expr = invokeStmt.getInvokeExpr();
          // only analyze constructor invokes
          if (expr instanceof SpecialInvokeExpr) {
            logger.debug("Checking invoke statement " + invokeStmt);
            Value end = expr.getArg(1);
            Abstract1 fallout = invokeToAbstract.get(invokeStmt);
            Lincons1 lincons1 = analysis.getLinConstraint(start, end, Lincons1.SUP);
            logger.debug("Constraint: " + lincons1);
            try {
              logger.debug("fallout before meet: " + fallout);
              fallout.meet(man, lincons1);
              logger.debug("fallout after meet: " + fallout);
              if (!fallout.isBottom(man))
                // start - end > 0 => property might not hold
                startEndOrder = false;
            } catch (ApronException e1) {
              e1.printStackTrace();
            }
          }
        }
      }
    }
    return startEndOrder;
  }

  @Override
  public boolean checkAfterStart() {
    if (numericalAnalysis.isEmpty()) {
      return true;
    }

    boolean afterStart = true;

    for (SootMethod method : this.numericalAnalysis.keySet()) {
      NumericalAnalysis analysis = this.numericalAnalysis.get(method);
      Manager man = analysis.man;
      Collection<EventInitializer> events = this.pointsTo.getInitializers(method);
      Map<JInvokeStmt, Abstract1> invokeToAbstract = analysis.invokeToAbstract;

      for (EventInitializer event : events) {
        Value start = event.getStatement().getInvokeExpr().getArg(0);

        for (JInvokeStmt invokeStmt : event.getInvokes()) {
          InvokeExpr expr = invokeStmt.getInvokeExpr();
          // only analyze switchlights invokes
          if (expr instanceof VirtualInvokeExpr) {
            logger.debug("Checking event " + event.toString() + " with invoke statement " + invokeStmt);
            Value time = expr.getArg(0);
            Abstract1 fallout = invokeToAbstract.get(invokeStmt);
            Lincons1 lincons1 = analysis.getLinConstraint(start, time, Lincons1.SUP);
            try {
              fallout.meet(man, lincons1);
              // for (Abstract1 f : invokeToAbstract.values()) {
              // logger.debug("Fallout is " + f);
              // }
              logger.debug("fallout: " + fallout);
              if (!fallout.isBottom(man))
                // time - start > 0 => property might not hold
                afterStart = false;
            } catch (ApronException e1) {
              e1.printStackTrace();
            }
          }
        }
      }
    }
    return afterStart;
  }

  @Override
  public boolean checkBeforeEnd() {
    if (numericalAnalysis.isEmpty()) {
      return true;
    }

    boolean beforeEnd = true;

    for (SootMethod method : this.numericalAnalysis.keySet()) {
      NumericalAnalysis analysis = this.numericalAnalysis.get(method);
      Manager man = analysis.man;
      Collection<EventInitializer> events = this.pointsTo.getInitializers(method);
      Map<JInvokeStmt, Abstract1> invokeToAbstract = analysis.invokeToAbstract;

      for (EventInitializer event : events) {
        Value end = event.getStatement().getInvokeExpr().getArg(1);
        // logger.debug("End is " + end.toString());
        for (JInvokeStmt invokeStmt : event.getInvokes()) {
          InvokeExpr expr = invokeStmt.getInvokeExpr();
          // only analyze switchlights invokes
          if (expr instanceof VirtualInvokeExpr) {
            logger.debug("Checking event " + event.toString() + " with invoke statement " + invokeStmt);
            Value time = expr.getArg(0);
            Lincons1 lincons1 = analysis.getLinConstraint(time, end, Lincons1.SUP);
            Abstract1 fallout = invokeToAbstract.get(invokeStmt);
            try {
              fallout.meet(man, lincons1);
              logger.debug("fallout: " + fallout);
              if (!fallout.isBottom(man))
                // time - end > 0 => property might not hold
                beforeEnd = false;
            } catch (ApronException e1) {
              e1.printStackTrace();
            }
          }
        }
      }
    }
    return beforeEnd;
  }

  // TODO: MAYBE FILL THIS OUT: add convenience methods

}
