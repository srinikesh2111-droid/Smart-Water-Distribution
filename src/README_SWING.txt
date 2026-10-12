# Smart Water Distribution Management — Java Swing Upgrade

A Java-only desktop simulation for the fictional town of Neer Nagar. The upgraded Swing UI uses only the JDK and Java standard library. No JavaScript, HTML application, database, external framework, or internet connection is required.

## Requirements
- JDK 17 or newer recommended (Swing and the source use standard Java APIs).
- Windows: double-click `run-swing.bat`.
- Or run from the project folder:
  ```bash
  javac -encoding UTF-8 -d out src/*.java
  java -cp out SwingMain
  ```

## Keep the original console app
The original `src/Main.java` is retained. Run it with `run-console.bat`, or:
```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out Main
```
The legacy console app intentionally retains its original 8-area/8-pipeline Phase-1 sample. The upgraded Swing simulation uses 10 areas and 12 pipelines.

## Review demo
1. Open Dashboard. Point out the live total remaining supply (18,000 L initially), 2 reservoirs, 10 areas, 12 pipelines.
2. Open Allocate Water. Select A3 (Government Hospital), enter 2,500, and click Allocate water. The selected area is manual; reservoir choice is automatic and only a reachable reservoir can be selected.
3. Check Reservoirs on the Dashboard's live data? Use the Events & Supply panel / dashboard metric: the R1 remaining supply decreases by the amount actually delivered. The detailed remaining supply is also shown in the live reservoir table on Allocate Water? Use the tables and return to the dashboard after each action.
4. Enter a request larger than the available supply to show partial allocation and unmet litres.
5. Fail P9. A9 and A10 become unreachable from R1; an open FIFO maintenance ticket is created. Repair P9 and observe connectivity return; pending unmet requests are retried.
6. Use the priority and ID-order batch buttons to demonstrate both scheduling modes.
7. Use the preset critical-demand event, dry-reservoir test, supply adjustment, and daily reset buttons.
8. Open History & Maintenance to show requests, allocation outcomes, events, and maintenance tickets.

## Model / data structures
- `WaterNetwork`: graph stored as an adjacency list; BFS uses `ArrayDeque`, `HashSet`, and predecessor map to test reachability and return a path.
- `Reservoir`: encapsulated remaining supply, allocation cap, and daily reset.
- `DemandArea`: abstract base class; `CriticalInstitution`, `HighPriorityArea`, and `ResidentialArea` implement priority polymorphically.
- `WaterAllocationPolicy`: provides priority-based or deterministic ID-order batch ranking. Priority tie-break: area ID ascending.
- `WaterRequest`: tracks requested, delivered, and unmet litres.
- `MaintenanceRequest`: tracks pipeline fault and resolution. New tickets enter a FIFO queue.
- On failure/repair or supply updates, reachability is recalculated and pending/unmet requests are retried. Already delivered water is not rolled back when a pipeline later fails.
- Reservoir selection: among reservoirs that can currently reach the selected area, choose the one with the greatest remaining supply; tie-break by reservoir ID.

## Limitations (important for viva)
- This is a simulation: it models graph reachability, not physical pipe capacity, water pressure, leakage, flow rates, pump control, or real sensors.
- All values and events are in-memory sample data and reset when the app closes. No database or persistence.
- Batch allocation creates one request per area's configured sample daily demand each time the batch button is clicked; running it again creates a new set of requests.
- Daily reset restores each reservoir to capacity but intentionally does not erase prior allocation/history records.
- The original console app remains a Phase-1 fallback and does not include the new Swing allocation scheduler.
- Sample event log and allocation history are session-only.

## Test
Run `run-tests.bat`, or:
```bash
javac -encoding UTF-8 -d out src/*.java
java -ea -cp out ProjectSelfTest
```
