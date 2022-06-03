package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Loop_6Nested_Test {
  public static void m2(int i) {
    i = 64;
    Event e = new Event(0, i);
    for (int j = 0; j <= 2; j++) {
      for (int k = j; k <= 2; k++) {
        for (int l = k; l <= 2; l++) {
          for (int m = l; m <= 2; m++) {
            for (int n = m; n <= 2; n++) {
              for (int p = n; p <= 2; p++) {
                e.switchLights(j * k * l * m * n * p);
              }
            }
          }
        }
      }
    }
  }
}
