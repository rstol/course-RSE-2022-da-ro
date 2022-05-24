package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START UNSAFE
// BEFORE_END UNSAFE

public class If_Joins_Test_Unsafe {
    public static void m2(int i) {
        Event e = new Event(42, 43);
        if (2 * i + 1 > 82 && i <= 44) {
            if (i <= 128) {
                e.switchLights(i);
            }
        }
    }
}