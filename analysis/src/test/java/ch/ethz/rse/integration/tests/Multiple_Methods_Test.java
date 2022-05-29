package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Multiple_Methods_Test {
  public void m1(int i) {
    Event e = new Event(0, 50);
    Event e1 = new Event(0, 50);
    Event e2;
    if (i >= 0) {
      e2 = e;
    } else {
      e2 = e1;
    }
    if (i >= 0 && i <= 50) {
      e.switchLights(i);
      e2.switchLights(i);
    }
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