package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START UNSAFE
// BEFORE_END SAFE

public class Unreachable_Test_Unsound {
    public static void m2(int i) {
        Event e = new Event(42, 128);
        if (i >= 10) {
            if (i < 10) {
                //unreachable. If still evaluated, would introduce UNSOUND behaviour (validating BEFORE_START)
                e = new Event(0, 42);
            }
        }
        e.switchLights(2);
    }
}