package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START UNSAFE
// BEFORE_END UNSAFE

public class Uninitialized_Event_Test {
  public void m1(int i) {
    Event e = new Event(10, 10); // makes AFTER_START UNSAFE
    Event e1 = new Event(0, 0); // makes BEFORE_END UNSAFE
    Event e2;
    if (i >= 0) {
      e2 = e;
    } else {
      e2 = e1;
    }
    if (i >= 0 && i <= 10)
      e2.switchLights(i);
  }
}