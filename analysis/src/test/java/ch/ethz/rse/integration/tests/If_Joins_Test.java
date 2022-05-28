package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class If_Joins_Test {
  public static void m2(int i) {
    Event e = new Event(42, 43);
    if (i >= 42) {
      if (i < 44) {
        e.switchLights(i);
      }
    }
  }
}