package ch.ethz.rse.pointer;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.rse.utils.Constants;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.spark.pag.Node;

/**
 * Convenience class which helps determine the {@link EventInitializer}s
 * potentially used to create objects pointed to by a given variable
 */
public class PointsToInitializer {

  private static final Logger logger = LoggerFactory.getLogger(PointsToInitializer.class);

  /**
   * Internally used points-to analysis
   */
  private final PointsToAnalysisWrapper pointsTo;

  /**
   * class for which we are running points-to
   */
  private final SootClass c;

  /**
   * Maps abstract object indices to initializers
   */
  private final Map<Node, EventInitializer> initializers = new HashMap<Node, EventInitializer>();

  /**
   * All {@link EventInitializer}s, keyed by method
   */
  private final Multimap<SootMethod, EventInitializer> perMethod = HashMultimap.create();

  public PointsToInitializer(SootClass c) {
    this.c = c;
    logger.debug("Running points-to analysis on " + c.getName());
    this.pointsTo = new PointsToAnalysisWrapper(c);
    logger.debug("Analyzing initializers in " + c.getName());
    this.analyzeAllInitializers();
  }

  private void analyzeAllInitializers() {
    for (SootMethod method : this.c.getMethods()) {

      if (method.getName().contains("<init>")) {
        // skip constructor of the class
        continue;
      }
      logger.debug("Analyzing initializers in method: " + method.getName());

      // populate data structures perMethod and initializers
      analyzeMethod(method);
    }
  }

  /**
   * Convenience Method: Analyze initializers defined in body of method.
   * Assume: parameters of method cannot contain Event initializer statements
   *
   * @param method
   */
  private void analyzeMethod(SootMethod method) {
    for (Unit ut : method.retrieveActiveBody().getUnits()) {
      if (ut instanceof JInvokeStmt) {
        JInvokeStmt jInvStmt = (JInvokeStmt) ut;
        InvokeExpr invokeExpr = jInvStmt.getInvokeExpr();
        if (invokeExpr instanceof JSpecialInvokeExpr) {
          JSpecialInvokeExpr specialInvExpr = (JSpecialInvokeExpr) invokeExpr;
          Collection<Node> nodes = this.getAllocationNodes(specialInvExpr);
          for (Node node : nodes) {
            Value base = specialInvExpr.getBase();
            logger.debug("The base variable for the event initializer  " + specialInvExpr + " is " + base);
            int uniqueNumber = node.hashCode();
            // Assume: constructor Event takes as first argument (start) only integer
            // constants.
            Value arg0 = specialInvExpr.getArg(0);
            IntConstant start = (IntConstant) arg0;
            // logger.debug("The invoke expression has as first argument: " +
            // invokeExpr.getArg(0));
            EventInitializer event = new EventInitializer(jInvStmt, uniqueNumber, start.value, base.toString());
            this.initializers.put(node, event);
            this.perMethod.put(method, event);
          }
        }
      }
    }
  }

  public Collection<EventInitializer> getInitializers(SootMethod method) {
    return this.perMethod.get(method);
  }

  public List<EventInitializer> pointsTo(Local base) {
    Collection<Node> nodes = this.pointsTo.getNodes(base);
    List<EventInitializer> initializers = new LinkedList<EventInitializer>();
    for (Node node : nodes) {
      EventInitializer initializer = this.initializers.get(node);
      if (initializer != null) {
        // ignore nodes that were not initialized
        initializers.add(initializer);
      }
    }
    return initializers;
  }

  /**
   * Returns all allocation nodes that could correspond to the given
   * invokeExpression, which must be a call to Event init function
   * Note that more than one node can be returned.
   */
  public Collection<Node> getAllocationNodes(JSpecialInvokeExpr invokeExpr) {
    if (!isRelevantInit(invokeExpr)) {
      throw new RuntimeException(
          "Call to getAllocationNodes with " + invokeExpr.toString() + "which is not an init call for the Event class");
    }
    Local base = (Local) invokeExpr.getBase();
    Collection<Node> allocationNodes = this.pointsTo.getNodes(base);
    return allocationNodes;
  }

  public boolean isRelevantInit(JSpecialInvokeExpr invokeExpr) {
    Local base = (Local) invokeExpr.getBase();
    boolean isRelevant = base.getType().toString().equals(Constants.EventClassName);
    boolean isInit = invokeExpr.getMethod().getName().equals("<init>");
    return isRelevant && isInit;
  }
}
