# Task 8 verification
Source is the exact PDF Vehicle hierarchy/FeeDemo; main() is unchanged.
| Scenario | Expected |
|---|---|
| TwoWheeler, 130 min | TwoWheeler TN01A1 -> Rs 30.0 |
| Car, 130 min | Car TN02B2 -> Rs 90.0 |
| EV, 130 min + 10 kWh | ElectricCar TN03C3 -> Rs 210.0 |
| Same registration equality | true |
Actual output is recorded in output.txt.