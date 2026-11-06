# Task 3 verification
Source is the exact page 4 ParkingFacility program; main() is unchanged.
| Scenario | Expected |
|---|---|
| First three allocations | [0, 0], [0, 1], [1, 0] |
| Fourth allocation when full | null |
| Release occupied (0,1), repeat release, invalid floor | true false false |
| Allocate after release | [0, 1] |
Run output is recorded in output.txt after execution.