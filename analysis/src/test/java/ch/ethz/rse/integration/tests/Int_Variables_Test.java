package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START SAFE
// BEFORE_END UNSAFE

public class Int_Variables_Test {

  public static void m1(int i, int j) {
    int end = i;
    Event e = new Event(1, end);
    e.switchLights(3);
  }
}