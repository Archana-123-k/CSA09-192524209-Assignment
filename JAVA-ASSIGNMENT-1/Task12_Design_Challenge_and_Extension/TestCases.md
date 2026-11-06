# Task 12 executed test plan
| ID | Scenario | Expected | Actual | Status |
|---|---|---|---|---|
| 01 | Five unequal floor counts [2,3,1,2,4] | 12 spaces | 12 spaces | PASS |
| 02 | first jagged allocation | [0, 0] | [0, 0] | PASS |
| 03 | two-hour e-bike | configured ₹15 flat | ₹15.00 | PASS |
| 04 | van 08:00–10:00 | ₹30 + ₹60 = ₹90 | ₹90.00 | PASS |
| 05 | van 17:00–18:00 | ₹30 | ₹30.00 | PASS |
| 06 | van 16:00–18:00 (5 PM boundary) | ₹60 + ₹30 = ₹90 | ₹90.00 | PASS |
| 07 | visitor one-day pass covers visit | ₹0 parking | ₹0.00 | PASS |
| 08 | existing monthly pass regression | ₹0 parking | ₹0.00 | PASS |
| 09 | EV 1h + 2 kWh | ₹30 + ₹24 = ₹54 | ₹54.00 | PASS |

Actual output is retained in output.txt.