package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Linear_If_Test_Safe {
  public void m2(int i) {
      Event e = new Event(0, 8);
      if (2*i <= 12 && i >= 0) {
        e.switchLights(i + 2);
      }
  }
}
