package ch.ethz.rse.pointer;

import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.testing.VerificationTestCase;
import soot.SootClass;
import soot.SootHelper;
import soot.SootMethod;

/**
 * Sanity checks on points-to-initializer
 */
public class PointsToInitializerTest {

  @Test
  public void testInitializers() {
    String packageName = "ch.ethz.rse.integration.tests.Basic_Test_Safe";
    VerificationTestCase t = new VerificationTestCase(packageName, VerificationProperty.START_END_ORDER, true);
    SootClass sc = SootHelper.loadClassAndAnalyze(t.getTestClass());
    // run points-to initializer
    PointsToInitializer w = new PointsToInitializer(sc);

    // check that pointer indeed points to an EventInitializer object
    SootMethod m = sc.getMethodByName("m1");
    Collection<EventInitializer> initializers = w.getInitializers(m);
    Assertions.assertEquals(1, initializers.size());
  }

}