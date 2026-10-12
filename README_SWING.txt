# Smart Water Distribution — Java Swing Interface

This version adds a Swing desktop dashboard to the supplied Phase-1 Java project.

## Run
- Install JDK 17 or newer.
- Double-click `run-swing.bat`, or run from the project folder:
  ```
  javac -encoding UTF-8 src/*.java
  java -cp src SwingMain
  ```

## Existing logic preserved
The original `Main.java` console program is still present and can still be run with:
```
javac src/*.java
java -cp src Main
```

The Swing interface uses the same model classes and the same `WaterNetwork.isReachable()` BFS method. It provides:
- Dashboard summary
- Reservoir table
- Demand-area table
- Pipeline status table
- Connectivity checks
- Mark pipeline failed / repair pipeline actions

## Important scope
This is based on the supplied Phase-1 source. It does not implement water-allocation scheduling, PriorityQueue-based allocation, maintenance request queues, or persistent storage. Reservoir figures are configured capacities/daily supply values from the current source, not live sensor readings.
