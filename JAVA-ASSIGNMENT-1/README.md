# JAVA-ASSIGNMENT-1 — Campus Smart-Parking System

**Assignment title:** Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System (CO1, 50 marks).

**Student:** [ENTER YOUR NAME]  
**Register number:** [ENTER YOUR REGISTER NUMBER]  
**GitHub repository:** `CSA09-<RegisterNumber>-Assignment1`  
**GitHub URL:** [ADD YOUR WORKING REPOSITORY LINK]  
**Java used for verification:** OpenJDK/Oracle Java 27 in this environment; assignment sources target Java 17-compatible syntax.

## Assignment structure

```text
JAVA-ASSIGNMENT-1/
├── Task01_Understanding_the_Problem/
├── Task02_Data_Representation/
├── Task03_Control_Logic_and_Slot_Allocation/
├── Task04_Classes_Objects_and_Constructors/
├── Task05_Static_Members_and_Object_Lifecycle/
├── Task06_Inheritance_and_Class_Hierarchy/
├── Task07_Method_Overriding/
├── Task08_Polymorphism_and_Parking_Fees/
├── Task09_Abstraction_and_Interfaces/
├── Task10_Packages_Access_Modifiers_and_Encapsulation/
├── Task11_Complete_Implementation_and_Testing/
├── Task12_Design_Challenge_and_Extension/
├── Complete_Code_Programs/
├── Final_Report/
├── UML_Diagrams/
├── Test_Results/
├── Database/
└── pom.xml
```

Each task folder has its own PDF-aligned Answer.txt, Explanation.md, Complexity.md, README.md, sample inputs/outputs where relevant, and Java source where relevant. The exact complete-code exercises live as ParkingFacility.java and FeeDemo.java in their corresponding task folders; their `main()` methods are preserved.

## Compile and run individual tasks

Use PowerShell from this folder. For each task, compile its source and execute the class named in that task's README. Examples:

```powershell
Set-Location Task03_Control_Logic_and_Slot_Allocation
javac ParkingFacility.java
java ParkingFacility
Set-Location ..\Task08_Polymorphism_and_Parking_Fees
javac FeeDemo.java
java FeeDemo
```

For tasks whose executable is Main.java:

```powershell
Set-Location Task11_Complete_Implementation_and_Testing
javac Main.java
java Main
```

Each task is compiled in its own directory because several standalone exercises intentionally use the same simple class names. Expected and executed outputs are retained in each task's output.txt. Java class files are build artifacts and should not be committed.

The task runner Maven pom selects a single task source directory. Example:

```powershell
mvn -Dtask.dir=Task11_Complete_Implementation_and_Testing compile exec:java
```

For Task 3 and Task 8, specify their entry class:

```powershell
mvn -Dtask.dir=Task03_Control_Logic_and_Slot_Allocation -Dtask.main=ParkingFacility compile exec:java
mvn -Dtask.dir=Task08_Polymorphism_and_Parking_Fees -Dtask.main=FeeDemo compile exec:java
```

## Existing web application in the workspace

The existing Spring Boot/MySQL web application remains in the workspace-level `src/`, `database/` and root Maven `pom.xml` next to this assignment folder. It provides the dashboard, REST backend and database schema. From the workspace parent, run `mvn test` and `mvn spring-boot:run`; set `DB_USERNAME` and `DB_PASSWORD` for local MySQL. The assignment's standalone task examples are separate and do not replace the application.

## GitHub submission checklist

1. Replace the name, register number and GitHub URL placeholders above.
2. Create/use the required repository name `CSA09-<RegisterNumber>-Assignment1`; do not commit credentials.
3. Add source, input and actual text-output evidence. Do not label an image a screenshot unless it was captured from a real execution.
4. Commit progressively, at least one commit per task (for example `Task 01: problem analysis`, …, `Task 12: design challenge`). Do not fabricate commit history or a repository URL.
5. Share the working repository with faculty as instructed by the PDF.

## Verification

The exact page 4 and page 8 programs are tested against the PDF outputs. Task 11 and Task 12 programs print expected/actual/status and stop on mismatch. The report links to all twelve individual answers. The official PDF is supplied separately at `C:\Users\DELL\Downloads\ASSIGNMENT 1.pdf`; include it in submission only if allowed by course rules.