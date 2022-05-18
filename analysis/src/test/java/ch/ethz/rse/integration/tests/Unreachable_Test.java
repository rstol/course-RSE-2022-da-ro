package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Unreachable_Test {
    public static void m2(int i) {
        Event e = new Event(0, 100);
        if (i >= 10 && i < 100) {
            e = new Event(10, 100);
            if (i < 10) {
                //unreachable
                e.switchLights(-2);
            }
            e.switchLights(i);
        }
        e.switchLights(42);
    }
}