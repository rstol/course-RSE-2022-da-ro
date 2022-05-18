package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER: SAFE
// AFTER_START: SAFE
// BEFORE_END: SAFE

public class BinOp_Test_Safe {
  public void m1() {
    Event e = new Event(0, 30);
    int a = 20;
    int b = 20;

    a = a * (-1); // -20
    a++; // -19
    a = a * 2; // -38
    a = a + 40; // 2
    a = a - 2; // 0
    e.switchLights(a);

    b = b - 25; // -5
    b = b * 3; // -15
    b = b + 45; // 30
    b = b - b; // 0
    b = b * a; // 0
    b = b + 31; // 31
    b--;
    e.switchLights(b);
  }
}
