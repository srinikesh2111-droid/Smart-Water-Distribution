# Smart Water Distribution Management — Review 2

## Requirements
- JDK 17+ (tested with OpenJDK 21)
- MySQL Server 8.x+ / MySQL Workbench for persistence
- Official MySQL Connector/J JAR in the `lib` folder for runtime database connectivity

## Configure MySQL
1. Download the platform-independent Connector/J archive from the official page: https://dev.mysql.com/downloads/connector/j/
2. Extract the `mysql-connector-j-*.jar` file into this project's `lib/` folder.
3. In MySQL Workbench, run `database_schema.sql`.
4. Copy `db.properties.example` to `db.properties` and enter your local MySQL username/password. Do not share `db.properties` or commit it to Git.
5. If you use Maven, the `pom.xml` declares MySQL Connector/J 9.5.0. Run `mvn dependency:copy-dependencies -DoutputDirectory=lib` to fetch the runtime JAR, or download Connector/J from the official page below and place its JAR in `lib/`.
6. Run `run-db-check.bat` to verify the JDBC connection, then run `run-swing.bat`, or from VS Code PowerShell run:
   ```powershell
   javac -encoding UTF-8 -d out src/*.java
   java -cp "out;lib/*" SwingMain
   ```

## Database behaviour
- On first successful connection, the application seeds the reservoir, demand-area and pipeline tables from the built-in Java model.
- On later launches, it restores remaining reservoir supply, pipeline status, water requests, allocation history, event history, and maintenance tickets.
- Model changes are saved in a transaction after dashboard refresh. If no `db.properties` is present, the app stays in in-memory mode. If MySQL is configured but unreachable, it warns and continues in memory for demo resilience.
- Database operations use JDBC `PreparedStatement` and a transaction. Do not use a privileged production database account for this student demo.

## Test
```powershell
javac -encoding UTF-8 -d out src/*.java
java -ea -cp "out;lib/*" ProjectSelfTest
java -cp "out;lib/*" DatabaseConnectionTest
```
The regression test does not require a running MySQL server because it tests the model independently. The live JDBC integration must be verified against your local MySQL Server after Connector/J and credentials are configured.

## Console fallback
```powershell
javac -encoding UTF-8 -d out src/*.java
java -cp out Main
```

See `DATABASE_SETUP.txt` for a concise Windows/VS Code setup guide.
