package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START UNSAFE
// BEFORE_END UNSAFE

public class Multiple_Methods_Test {
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

  public void m2(int i) {
    Event e = new Event(0, 50);
    Event e1 = new Event(0, 50);
    Event e2;
    if (i > 50) {
      e2 = e1;
    } else {
      e2 = e;
    }
    if (i >= 0 && i <= 20)
      e2.switchLights(i);
  }
}
