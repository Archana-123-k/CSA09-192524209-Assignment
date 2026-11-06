# Task 11 executed test plan
Main.java prints Expected, Actual and PASS/FAIL for each check; any mismatch throws an assertion error.
| ID | Scenario | Expected | Actual | Status |
|---|---|---|---|---|
| 01 | bike entry first slot | 0-0 | 0-0 | PASS |
| 02 | bike 60 minutes | ₹10.00 | ₹10.00 | PASS |
| 03 | car 61 minutes | ₹60.00 | ₹60.00 | PASS |
| 04 | EV 130 minutes + 10 kWh | ₹210.00 | ₹210.00 | PASS |
| 05 | monthly pass | ₹0.00 | ₹0.00 | PASS |
| 06 | manually computed daily total | ₹280.00 | ₹280.00 | PASS |
| 07 | zero-minute fee | ₹0.00 | ₹0.00 | PASS |
| 08 | exactly 60-minute car | ₹30.00 | ₹30.00 | PASS |
| 09 | last slot consumed | 0 available | 0 available | PASS |
| 10 | full facility entry | no free slot | no free slot | PASS |
| 11 | duplicate active registration | duplicate registration | duplicate registration | PASS |
| 12 | unknown AIRCRAFT type | unknown vehicle type | unknown vehicle type | PASS |
| 13 | invalid slot index | false | false | PASS |

Actuals were observed in the executed Java run and are retained in output.txt.