package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Test_3 {
  public void m1() {
    int start = 5;
    int i = 10;
    while (i > 3) {
      Event e = new Event(start, i);
      i--;
    }
  }
}
