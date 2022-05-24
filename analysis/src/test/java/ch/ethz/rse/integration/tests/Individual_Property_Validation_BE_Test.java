package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START UNSAFE
// BEFORE_END SAFE

public class Individual_Property_Validation_BE_Test {
    public static void m1() {
        Event e1 = new Event(10, 0);
        e1.switchLights(-5);
    }
}