
package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START UNSAFE
// BEFORE_END SAFE

public class Loop_Test2 {
  public static void m1(int i) {
    Event e = new Event(0, 200);
    for (int j = 0; j <= 100; j++) {
      e.switchLights(j);
    }
    for (int k = 200; k > i; k--) {
      e.switchLights(k);
    }
    e.switchLights(0);
    e.switchLights(200);
  }
}
