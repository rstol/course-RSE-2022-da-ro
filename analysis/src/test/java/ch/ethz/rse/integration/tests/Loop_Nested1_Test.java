package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Loop_Nested1_Test {
  public static void m1(int i) {
    Event e = new Event(0, 100);

    for (int j = 0; j <= 10; j++) {
      e.switchLights(j);
      for (int k = j; k <= 10; k++) {
        e.switchLights(k);
      }
    }

    e.switchLights(90);
    e.switchLights(42);
  }
}
