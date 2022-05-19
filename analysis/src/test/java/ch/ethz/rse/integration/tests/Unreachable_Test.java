package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Unreachable_Test {
    public static void m2(int i) {
        Event e = new Event(42, 128);
        if (i >= 42) {
            if (i < 16) {
                //unreachable, would violate AFTER_START and START_END_ORDER if not
                Event u = new Event(128, 42);
                e.switchLights(2);
            }
        }
    }
}