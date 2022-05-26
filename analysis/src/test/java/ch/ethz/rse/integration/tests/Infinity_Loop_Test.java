package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END UNSAFE

public class Infinity_Loop_Test {
  public void m1(int i) {
    int time = 200;
    Event e = new Event(0, time);
    while (true) {
      time = time - i;
      if (time < 0) {
        break;
      }
      e.switchLights(time);
    }
  }
}