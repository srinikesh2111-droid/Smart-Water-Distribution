# Smart Water Distribution Management

Mini Project - Phase 1

## Current implementation
- Reservoir class with encapsulation
- Pipeline class with working/failed state
- DemandArea base class
- CriticalInstitution and ResidentialArea inheritance
- WaterNetwork using HashMap + ArrayList adjacency list
- BFS using ArrayDeque + HashSet
- Pipeline failure-aware connectivity checking
- Sample Main program

## Run
From the project folder:

```bash
javac src/*.java
java -cp src Main
```

Expected output:
```text
Can R1 reach A3? true
Can R1 reach A4? true
```

## Next phase
Add:
- High/Normal priorities
- WaterRequest
- PriorityQueue
- Partial water allocation
- Unmet litres
- MaintenanceRequest
- More sample areas and pipelines
- Menu-driven console application
