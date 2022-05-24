package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// / expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END UNSAFE

public class Assign_Test {
  public static void m1(int i) {
    Event e = new Event(0, 0);
    Event e1 = new Event(0, 1);
    Event e2;

    if (i == 0) {
      e2 = e;
    } else if (i == 1) {
      e = e1;
      e2 = e1;
    } else {
      return;
    }
    e.switchLights(i);
    e2.switchLights(i);
  }
}
