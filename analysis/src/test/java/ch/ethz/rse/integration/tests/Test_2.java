package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END UNSAFE

public class Test_2 {
  public void m1() {
    Event e = new Event(0, 10);
    int i = 0;
    while (i != 11) {
      e.switchLights(i);
      i++;
    }
  }
}
