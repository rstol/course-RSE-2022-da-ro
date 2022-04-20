package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START UNSAFE
// BEFORE_END UNSAFE

public class START_END_ORDER_Test_Safe {
  Event e = new Event(2, 4);
}
