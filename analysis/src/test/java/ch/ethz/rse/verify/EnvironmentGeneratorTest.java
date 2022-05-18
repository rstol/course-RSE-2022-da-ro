package ch.ethz.rse.verify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import apron.Environment;
import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.pointer.PointsToInitializer;
import ch.ethz.rse.testing.VerificationTestCase;
import soot.SootClass;
import soot.SootHelper;
import soot.SootMethod;

/**
 * Sanity checks on EnvironmentGenerator
 */
public class EnvironmentGeneratorTest {

  @Test
  public void testIntVariables() {
    String packageName = "ch.ethz.rse.integration.tests.Int_Variables_Test";
    VerificationTestCase t = new VerificationTestCase(packageName, VerificationProperty.START_END_ORDER, true);
    SootClass sc = SootHelper.loadClassAndAnalyze(t.getTestClass());
    // generate environment
    PointsToInitializer pointsTo = new PointsToInitializer(sc);
    SootMethod method = sc.getMethodByName("m1");
    Environment env = new EnvironmentGenerator(method, pointsTo).getEnvironment();

    // check that pointer indeed points to an abstract object
    int intDim = env.getDimension().intDim;
    Assertions.assertEquals(3, intDim);
  }
}
