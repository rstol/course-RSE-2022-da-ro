package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER: SAFE
// AFTER_START: SAFE
// BEFORE_END: SAFE

public class Loop_Break_Test {
  public void m1() {
    Event e = new Event(0, 50);
    for (int i = 0; i < 200; i++) {
      if (i >= 50) {
        break;
      }
      e.switchLights(i);
    }
  }
}
