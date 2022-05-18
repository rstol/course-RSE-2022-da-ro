package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Test_1 {
  public void m1(int i) {
    if (0 <= i && i <= 8) {
      Event e = new Event(0, i);
      if (i < 4) {
        e.switchLights(i);
      } else {
        e.switchLights(i);
      }
    }

  }

}
