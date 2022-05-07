package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Int_Variables_Test_Safe {

  public static void m1(int i, int j) {
    int end = i;
    Event e = new Event(1, end);
    e.switchLights(3);
  }
}