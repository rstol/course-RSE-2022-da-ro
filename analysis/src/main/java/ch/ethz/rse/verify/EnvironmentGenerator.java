package ch.ethz.rse.verify;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterables;

import org.slf4j.Logger; //NEW
import org.slf4j.LoggerFactory; //NEW

import apron.Environment;
import ch.ethz.rse.pointer.PointsToInitializer;
import soot.Local;
import soot.SootHelper;
import soot.SootMethod;

/**
 * Generates an environment which holds all variable names needed for the
 * numerical analysis of a method
 *
 */
public class EnvironmentGenerator {

  private final SootMethod method;

  private final PointsToInitializer pointsTo;

  private static final Logger logger = LoggerFactory.getLogger(EnvironmentGenerator.class);

  /**
   * List of names for integer variables relevant when analyzing the program
   */
  private List<String> ints = new LinkedList<String>();

  private final Environment env;

  /**
   *
   * @param method
   */
  public EnvironmentGenerator(SootMethod method, PointsToInitializer pointsTo) {
    this.method = method;
    this.pointsTo = pointsTo;

    this.populateInts();

    String ints_arr[] = Iterables.toArray(this.ints, String.class);

    String reals[] = {}; // we are not analyzing real numbers
    this.env = new Environment(ints_arr, reals);
  }

  public Environment getEnvironment() {
    return this.env;
  }

  // convenience method
  private void populateInts() {
    for (Local l : method.retrieveActiveBody().getLocals()) {
      if (SootHelper.isIntValue(l)) {
        ints.add(l.getName());
      } else {
        // Add non-int variables to the environment as well to track the intervals of
        // Event class variables
        ints.add(l.getName());
        // logger.debug("Local is not int and has type: " + l.getType());
      }
    }
  }
}
