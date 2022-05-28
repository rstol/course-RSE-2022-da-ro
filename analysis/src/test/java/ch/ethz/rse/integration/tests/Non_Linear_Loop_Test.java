package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START UNSAFE
// BEFORE_END SAFE

public class Non_Linear_Loop_Test {
  public void m2(int i) {
    Event e = new Event(0, 20);
    int k = i * i;
    while (k <= 20) {
      e.switchLights(k);
      i++;
      k = i * i;
    }
  }
}
