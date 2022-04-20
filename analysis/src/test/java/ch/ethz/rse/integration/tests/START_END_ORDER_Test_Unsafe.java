package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START UNSAFE
// BEFORE_END UNSAFE

public class START_END_ORDER_Test_Unsafe {
  Event e = new Event(3, 2);
}
