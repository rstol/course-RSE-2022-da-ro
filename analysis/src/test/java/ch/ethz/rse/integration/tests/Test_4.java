package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Test_4 {
  public static void m1(int i) {
    Event e=new Event(0,100);
    if(i>0) e=new Event(0,400);
    e.switchLights(50);
  }
}
