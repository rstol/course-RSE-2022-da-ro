package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Do_While_Infinity_Test {
  public static void m2(int i) {
    i = 0;
    int j = 0;
    do {
      j++;
      Event e1 = new Event(0, j);
      e1.switchLights(i + 1);
      i++;
      e1 = new Event(0, i);
      e1.switchLights(j);
    } while (true);
  }
}
