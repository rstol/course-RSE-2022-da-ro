package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class If_Stmt_Test_Safe {
  public void m2(int i, int j) {
    if (i > 2) {
      Event e = new Event(2, i);
      e.switchLights(i);
    }
  }
}
