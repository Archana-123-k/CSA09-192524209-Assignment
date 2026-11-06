# Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System

## Cover Page

**Course outcome:** CO1 — Java fundamentals and object-oriented programming  
**Assignment:** Assignment 1 — Individual  
**Total:** 50 marks  
**Student name:** [ENTER NAME]  
**Register number:** [ENTER REGISTER NUMBER]  
**Submission date:** [ENTER DATE]  
**GitHub repository:** `CSA09-<RegisterNumber>-Assignment1`  
**Repository URL:** [ADD WORKING LINK]

> Do not submit while these student/GitHub placeholders remain. The assignment PDF states a working GitHub link is mandatory. Commit work progressively; no commit history or screenshots are fabricated here.

## Table of Contents

1. [Task 1 — Understand the Problem and Identify the Objects](#task-1--understand-the-problem-and-identify-the-objects)
2. [Task 2 — Choose the Data Representation](#task-2--choose-the-data-representation)
3. [Task 3 — Design the Control Logic](#task-3--design-the-control-logic)
4. [Task 4 — Design Classes, Objects and Constructors](#task-4--design-classes-objects-and-constructors)
5. [Task 5 — Apply Static Members and the Object Life Cycle](#task-5--apply-static-members-and-the-object-life-cycle)
6. [Task 6 — Design the Inheritance Hierarchy](#task-6--design-the-inheritance-hierarchy)
7. [Task 7 — Override and Specialise Behaviour](#task-7--override-and-specialise-behaviour)
8. [Task 8 — Apply Polymorphism](#task-8--apply-polymorphism)
9. [Task 9 — Introduce Abstraction](#task-9--introduce-abstraction)
10. [Task 10 — Enforce Encapsulation](#task-10--enforce-encapsulation)
11. [Task 11 — Implement, Test and Defend the Design](#task-11--implement-test-and-defend-the-design)
12. [Task 12 — Re-evaluate and Adapt the Design](#task-12--re-evaluate-and-adapt-the-design)
13. [Conclusion](#conclusion)

This report summarizes all PDF tasks. The exact task prompts, full answers and alternative-concept discussions are in each linked `Answer.txt`; individual executable code and actual outputs are in the corresponding task folders.

## Task 1 — Understand the Problem and Identify the Objects

**Original task:** Analyse the parking problem, inputs/outputs, candidate nouns/classes and verbs/methods, class responsibilities, business rules and assumptions. Justify each class. Compare vehicle objects with parallel arrays for search and adding an attribute; analyse time and space. **Evidence:** analysis and assumptions.

**Answer summary:** Model Facility, Slot, Vehicle, Ticket, User, Pass, PricingRule, Payment and Report because each owns separate state/invariants. Entry receives registration/type/user/pass/entry time; exit receives ticket/registration/exit and possibly EV energy. Output includes location, ticket, fee and reports. Enforce one active visit per registration, no double occupancy, unique ticket IDs and atomic valid entry/exit. Parallel-array search and object-array search are both O(n); adding an attribute to parallel arrays requires synchronizing each operation. A registration-keyed map is expected O(1), with O(n) storage. Assumptions and omitted PDF rate values are stated in the task answer.

[Full Task 1 answer](../Task01_Understanding_the_Problem/Answer.txt)

## Task 2 — Choose the Data Representation

**Original task:** Justify field data types; choose 1D/2D/jagged floor representation; describe conversions/casting and precision risk; choose Scanner or CLI; show an integer-division/cast fee error and correction. Compare unequal floors with flattened array or ArrayList<String[]>; analyse lookup and space. **Evidence:** justified types and casting risk.

**Answer summary:** Use int for indexes, String for registration, LocalDateTime for time, long for elapsed units, enum for type, boolean for occupancy, BigDecimal for money. Jagged String[][] matches uneven capacities. 75/60 truncates to 1; casting after division remains wrong. Use ceiling integer arithmetic. Known cell access is O(1), free-slot search O(N), storage O(N); flattened unequal capacities need prefix offsets.

[Full Task 2 answer](../Task02_Data_Representation/Answer.txt)

## Task 3 — Design the Control Logic

**Original task:** Explain allocation search/stop, fee slabs with if/switch, break/continue/return, full-facility behavior; provide SIMATS-style pseudocode for allocation and fees; complete the page 4 ParkingFacility without changing main; compare scan with stack-based free-slot allocation. **Evidence:** pseudocode.

**Answer summary:** Scan floor-major/slot-major and return on first free cell; return null if full. Validate release indices before accessing a jagged array. Fee pseudocode separates pass, type, started hours, optional daily maximum and EV charging. Scan allocation O(N), release O(1); stack pop/push O(1) with O(N) extra storage. The exact completed `ParkingFacility.java` retains the printed PDF main.

**PDF expected and executed output:**

```text
[0, 0]
[0, 1]
[1, 0]
null
true false false
[0, 1]
```

[Full Task 3 answer](../Task03_Control_Logic_and_Slot_Allocation/Answer.txt) · [source](../Task03_Control_Logic_and_Slot_Allocation/ParkingFacility.java) · [actual output](../Task03_Control_Logic_and_Slot_Allocation/output.txt)

## Task 4 — Design Classes, Objects and Constructors

**Original task:** Explain fields/methods; default, parameterized and overloaded constructors; this and chaining; copy constructor for duplicate ticket; stack/heap. Compare setter construction, constructors and static factories for validity and n-object time/space. **Evidence:** class definitions and object creation.

**Answer summary:** Constructor validation establishes valid state, `this(...)` removes duplicate initialization, and unique ticket IDs must not be copied. Local references refer to heap objects; unreachable objects are merely eligible for GC. All n-object construction strategies are O(n) total time and space, but setters may expose invalid intermediate state.

[Full Task 4 answer](../Task04_Classes_Objects_and_Constructors/Answer.txt)

## Task 5 — Apply Static Members and the Object Life Cycle

**Original task:** Justify static counter/occupancy, static methods/block, ticket after exit/GC, object arguments/return. Compare static vs Facility instance counter for k facilities and n tickets. **Evidence:** static members and lifecycle.

**Answer summary:** Ticket sequence is class-wide; occupancy is per facility. A global counter uses O(1) shared counter space and avoids north/south ID collisions. Per-facility counters use O(k) counter storage and require qualified IDs. ID allocation is O(1); n tickets O(n). GC eligibility is nondeterministic.

[Full Task 5 answer](../Task05_Static_Members_and_Object_Lifecycle/Answer.txt)

## Task 6 — Design the Inheritance Hierarchy

**Original task:** Design vehicle and possibly user inheritance; distinguish single, multilevel and hierarchical inheritance; inherited/added members; super; trace ElectricCar constructor order; draw UML. Compare composition with inheritance for adding types, fee time and per-vehicle space. **Evidence:** hierarchy, UML and trace.

**Answer summary:** Vehicle is the superclass; TwoWheeler/Car form hierarchical inheritance and ElectricCar extends Car (multilevel). Student/Staff/Visitor can extend ParkingUser. `super(...)` initializes base state first. Composition with a PricingRule changes policies without subclass growth; both direct strategy and dispatch fee calculation are O(1). Diagrams are in UML_Diagrams.

[Full Task 6 answer](../Task06_Inheritance_and_Class_Hierarchy/Answer.txt) · [UML](../UML_Diagrams/vehicle-inheritance.mmd)

## Task 7 — Override and Specialise Behaviour

**Original task:** Identify overridden behavior; explain @Override rules, final choices, and toString/equals/hashCode identity by registration. Compare one switch-based calculator for k types in runtime and change impact. **Evidence:** correct overrides.

**Answer summary:** Override fee/rate behavior with matching signatures and non-reduced access. final protects identity and invariant code. equals/hashCode must use consistent identity. Switch and virtual dispatch are constant-time in typical JVM enum/dispatch implementations; polymorphism limits central edits when adding a type.

[Full Task 7 answer](../Task07_Method_Overriding/Answer.txt)

## Task 8 — Apply Polymorphism

**Original task:** Show overload selection, Vehicle array upcasting, dynamic dispatch, avoiding type branches and trace declared/actual types. Complete exact Vehicle/Chargeable/FeeDemo code pages 7–8; do not change main. Explain last equals output. Compare instanceof chain for k types. **Evidence:** running overload/dispatch.

**Answer summary:** Vehicle reference declared type can point to ElectricCar; virtual `ratePerHour()` dispatches to car rate. For 130 minutes, billable hours = 3; fees are 30, 90, 90+120=210. Equals is true because both objects have registration TN02B2; that matches a globally unique registration identity. `FeeDemo.java` preserves PDF main and fills only TODO bodies.

**PDF expected and executed output:**

```text
TwoWheeler TN01A1 -> Rs 30.0
Car TN02B2 -> Rs 90.0
ElectricCar TN03C3 -> Rs 210.0
true
```

[Full Task 8 answer](../Task08_Polymorphism_and_Parking_Fees/Answer.txt) · [source](../Task08_Polymorphism_and_Parking_Fees/FeeDemo.java) · [actual output](../Task08_Polymorphism_and_Parking_Fees/output.txt)

## Task 9 — Introduce Abstraction

**Original task:** Choose abstract class/method and capability interface; justify; explain multiple interfaces, default/static methods. Compare changing Vehicle to interface and Chargeable to abstract class, identifying loss of constructors/state/multiple inheritance and complexity. **Evidence:** justified abstraction.

**Answer summary:** Abstract Vehicle owns shared registration and constructor; Chargeable is an optional EV capability; classes may implement multiple capabilities. Reversing the choices loses ordinary Vehicle constructors/state and restricts a class to one superclass. Runtime complexity remains O(1).

[Full Task 9 answer](../Task09_Abstraction_and_Interfaces/Answer.txt)

## Task 10 — Enforce Encapsulation through Packages and Access Modifiers

**Original task:** Define package/access structure; explain private/default/protected/public; validated getters/setters; wrapper/autoboxing and Integer == risks. Compare public fields with repeated caller-side validation. **Evidence:** protected state and validated API.

**Answer summary:** Package model/service/pricing/persistence/app boundaries. Keep state private; expose validated APIs; use protected only for extension and package-private for internal helpers. Wrapper null/unboxing and reference equality are risks. Scalar validation is O(1), but public-field approach repeats checks at call sites and can bypass invariants.

[Full Task 10 answer](../Task10_Packages_Access_Modifiers_and_Encapsulation/Answer.txt)

## Task 11 — Implement, Test and Defend the Design

**Original task:** Implement and test normal vehicle entry/exit, last slot, zero and exact fee boundary, unknown type, duplicate registration, invalid slot; manually calculate a day's collection; evaluate OOP and one limitation. Compare array, ArrayList and HashMap exit lookup/space. **Evidence:** actual test table and reasoned evaluation.

**Manual collection:** bike 60m = ₹10; car 61m = ₹60; EV 130m + 10kWh = ₹90+₹120=₹210; monthly-pass car = ₹0. Total expected ₹280; the executed program reports ₹280.

| Test | Expected | Actual | Status |
|---|---|---|---|
| Two-wheeler entry and 60m exit | first slot; ₹10.00 | first slot; ₹10.00 | PASS |
| Car 61m | ₹60.00 | ₹60.00 | PASS |
| EV 130m + 10kWh | ₹210.00 | ₹210.00 | PASS |
| Monthly pass | ₹0.00 | ₹0.00 | PASS |
| Daily collection | ₹280.00 | ₹280.00 | PASS |
| Zero-minute parking | ₹0.00 | ₹0.00 | PASS |
| Car exactly 60m | ₹30.00 | ₹30.00 | PASS |
| Last free slot | 0 spaces remain | 0 spaces remain | PASS |
| Full / duplicate / unknown type | rejected | rejected | PASS |
| Invalid slot | false | false | PASS |

Array and ArrayList lookup by registration are O(n); HashMap is expected O(1) and O(n) storage. Fixed array is the PDF baseline and is easy to understand; keyed map/database is the production improvement. The executed table is [Task 11 output](../Task11_Complete_Implementation_and_Testing/output.txt).

## Task 12 — Re-evaluate and Adapt the Design

**Original task:** Day-4 changes: e-bike flat rate, delivery van double car rate 9–5, visitor one-day pass, five floors with varying slot counts. Complete A impact (2), B evaluation (2), C adaptation (2), D evidence-based defense (2), including changed classes/data, retained/modified/redesigned decision, revised classes/working code and tests/trade-offs.

**A — Impact:** Equal-floor formulas/rectangular arrays and central rate switches break; jagged arrays/configuration, pricing, pass, category selection need adaptation. **B — Evaluation:** Retain stable ticket/slot core; central switch spreads changes; strategy isolates pricing. **C — Decision:** Modify with PricingRule strategy, Pass interface and five jagged floor arrays; one-day validity <=24h. **D — Defence:** E-bike, peak van, visitor pass, unequal floors and EV regression are executed. Search O(N), storage O(N); van O(h) started segments; checking p passes O(p). Trade-off is more strategy classes for less central coupling.

| Test | Expected | Actual | Status |
|---|---|---|---|
| Five unequal floor counts [2,3,1,2,4] | 12 | 12 | PASS |
| First jagged allocation | [0,0] | [0,0] | PASS |
| E-bike flat charge | ₹15.00 | ₹15.00 | PASS |
| Van 08:00–10:00 | ₹90.00 | ₹90.00 | PASS |
| Van 17:00–18:00 | ₹30.00 | ₹30.00 | PASS |
| Visitor pass covers visit | ₹0 parking | ₹0 parking | PASS |
| EV 1h + 2kWh | ₹54.00 | ₹54.00 | PASS |

[Full Task 12 answer](../Task12_Design_Challenge_and_Extension/Answer.txt) · [actual output](../Task12_Design_Challenge_and_Extension/output.txt) · [revised UML](../Task12_Design_Challenge_and_Extension/revised-design.mmd)

## UML and architecture

The inheritance diagram is [vehicle-inheritance.mmd](../UML_Diagrams/vehicle-inheritance.mmd); the revised design and relationships are [revised-design.mmd](../Task12_Design_Challenge_and_Extension/revised-design.mmd); the web architecture is [system-architecture.mmd](../UML_Diagrams/system-architecture.mmd). GitHub renders Mermaid diagrams.

## Conclusion

The assignment applies data representation, control flow, constructors, static state, inheritance, overriding, polymorphism, abstraction, interfaces and encapsulation. The two exact PDF programs match their supplied expected output. Task 11 and Task 12 tests are executed and printed expected/actual comparisons. A strategy and pass interface adapt the model to the Day-4 requirements while retaining the tested slot/ticket foundation. Before submission, the student must insert their own name, register number and working GitHub link, create progressive commits, and be prepared to explain the code in the viva.