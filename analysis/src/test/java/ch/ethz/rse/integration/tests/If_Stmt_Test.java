package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START UNSAFE
// BEFORE_END SAFE

public class If_Stmt_Test {
  public void m2(int i, int j) {
    if (i > 2) {
      Event e1 = new Event(2, i);
      Event e2 = new Event(5, i);
      e1 = e2;
      e1.switchLights(i);
    }
  }
}
