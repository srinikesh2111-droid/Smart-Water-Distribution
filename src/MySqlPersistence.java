import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Optional MySQL persistence for the Swing application. Requires MySQL Connector/J at runtime. */
public final class MySqlPersistence {
    private final Properties properties = new Properties();
    private boolean enabled;
    private String status = "Database disabled: db.properties was not found; using in-memory mode.";

    public MySqlPersistence() {
        try (FileInputStream in = new FileInputStream("db.properties")) {
            properties.load(in);
            enabled = true;
            status = "Database configuration loaded; checking MySQL connection...";
        } catch (IOException ignored) {
            enabled = false;
        }
    }

    public boolean isEnabled() { return enabled; }
    public String getStatus() { return status; }

    /** Explicit connection smoke test; requires db.properties and Connector/J on the runtime classpath. */
    public String testConnection() {
        if (!enabled) return "NOT CONFIGURED: copy db.properties.example to db.properties first.";
        try (Connection c = connect(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT 1")) {
            return rs.next() && rs.getInt(1) == 1 ? "PASS: MySQL connection and SELECT 1 succeeded." : "FAIL: unexpected SELECT 1 result.";
        } catch (Exception ex) {
            return "FAIL: " + safeMessage(ex);
        }
    }

    private Connection connect() throws SQLException {
        String url = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/smart_water_distribution?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true").trim();
        String user = properties.getProperty("db.user", "root").trim();
        String password = properties.getProperty("db.password", "");
        return DriverManager.getConnection(url, user, password);
    }

    public synchronized void initializeAndLoad(WaterNetwork network, List<WaterRequest> requests,
            List<String> allocationHistory, List<String> eventHistory,
            Queue<MaintenanceRequest> maintenanceQueue, List<MaintenanceRequest> maintenanceHistory) {
        if (!enabled) return;
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                createTables(c);
                if (count(c, "reservoir_state") == 0) {
                    seedNetwork(c, network);
                } else {
                    loadNetworkState(c, network);
                    loadRequests(c, requests);
                    loadMaintenance(c, maintenanceQueue, maintenanceHistory);
                    loadTextHistory(c, "allocation_history", "entry_text", allocationHistory);
                    loadTextHistory(c, "event_history", "entry_text", eventHistory);
                }
                c.commit();
                status = "MySQL connected · state and history loaded.";
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception ex) {
            status = "MySQL unavailable; running in memory. " + safeMessage(ex);
        }
    }

    public synchronized void saveSnapshot(WaterNetwork network, List<WaterRequest> requests,
            List<String> allocationHistory, List<String> eventHistory,
            List<MaintenanceRequest> maintenanceHistory) {
        if (!enabled) return;
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                try (Statement st = c.createStatement()) {
                    st.executeUpdate("DELETE FROM reservoir_state");
                    st.executeUpdate("DELETE FROM demand_area");
                    st.executeUpdate("DELETE FROM pipeline_state");
                    st.executeUpdate("DELETE FROM water_request");
                    st.executeUpdate("DELETE FROM maintenance_request");
                    st.executeUpdate("DELETE FROM allocation_history");
                    st.executeUpdate("DELETE FROM event_history");
                }
                saveNetwork(c, network);
                saveRequests(c, requests);
                saveMaintenance(c, maintenanceHistory);
                saveTextHistory(c, "allocation_history", allocationHistory);
                saveTextHistory(c, "event_history", eventHistory);
                c.commit();
                status = "MySQL connected · latest changes saved.";
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception ex) {
            status = "MySQL save failed; current session remains in memory. " + safeMessage(ex);
        }
    }

    private void createTables(Connection c) throws SQLException {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS reservoir_state (id VARCHAR(20) PRIMARY KEY, name VARCHAR(120) NOT NULL, capacity INT NOT NULL, remaining_supply INT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS demand_area (id VARCHAR(20) PRIMARY KEY, name VARCHAR(120) NOT NULL, demand_litres INT NOT NULL, priority_label VARCHAR(30) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS pipeline_state (id VARCHAR(20) PRIMARY KEY, from_node VARCHAR(20) NOT NULL, to_node VARCHAR(20) NOT NULL, working BOOLEAN NOT NULL)",
            "CREATE TABLE IF NOT EXISTS water_request (id VARCHAR(40) PRIMARY KEY, area_id VARCHAR(20) NOT NULL, requested_litres INT NOT NULL, allocated_litres INT NOT NULL, unmet_litres INT NOT NULL, mode VARCHAR(80) NOT NULL, created_at VARCHAR(40) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS maintenance_request (id VARCHAR(40) PRIMARY KEY, pipeline_id VARCHAR(20) NOT NULL, description VARCHAR(255) NOT NULL, created_at VARCHAR(40) NOT NULL, resolved BOOLEAN NOT NULL)",
            "CREATE TABLE IF NOT EXISTS allocation_history (entry_id BIGINT AUTO_INCREMENT PRIMARY KEY, entry_text TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS event_history (entry_id BIGINT AUTO_INCREMENT PRIMARY KEY, entry_text TEXT NOT NULL)"
        };
        try (Statement st = c.createStatement()) { for (String sql : ddl) st.executeUpdate(sql); }
    }

    private int count(Connection c, String table) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) { rs.next(); return rs.getInt(1); }
    }

    private void seedNetwork(Connection c, WaterNetwork n) throws SQLException { saveNetwork(c, n); }

    private void saveNetwork(Connection c, WaterNetwork n) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO reservoir_state(id,name,capacity,remaining_supply) VALUES(?,?,?,?)")) {
            for (Reservoir r : n.getReservoirs()) { ps.setString(1,r.getId()); ps.setString(2,r.getName()); ps.setInt(3,r.getCapacity()); ps.setInt(4,r.getRemainingSupply()); ps.addBatch(); }
            ps.executeBatch();
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO demand_area(id,name,demand_litres,priority_label) VALUES(?,?,?,?)")) {
            for (DemandArea a : n.getAreas()) { ps.setString(1,a.getId()); ps.setString(2,a.getName()); ps.setInt(3,a.getDemand()); ps.setString(4,a.priorityLabel()); ps.addBatch(); }
            ps.executeBatch();
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO pipeline_state(id,from_node,to_node,working) VALUES(?,?,?,?)")) {
            for (Pipeline p : n.getPipelines()) { ps.setString(1,p.getId()); ps.setString(2,p.getFrom()); ps.setString(3,p.getTo()); ps.setBoolean(4,p.isWorking()); ps.addBatch(); }
            ps.executeBatch();
        }
    }

    private void loadNetworkState(Connection c, WaterNetwork n) throws SQLException {
        try (Statement st=c.createStatement(); ResultSet rs=st.executeQuery("SELECT id,remaining_supply FROM reservoir_state")) {
            while(rs.next()) { Reservoir r=n.getReservoir(rs.getString(1)); if(r!=null) r.setRemainingSupply(rs.getInt(2)); }
        }
        try (Statement st=c.createStatement(); ResultSet rs=st.executeQuery("SELECT id,working FROM pipeline_state")) {
            while(rs.next()) { Pipeline p=n.getPipeline(rs.getString(1)); if(p!=null) { if(rs.getBoolean(2)) p.repair(); else p.markFailed(); } }
        }
    }

    private void saveRequests(Connection c, List<WaterRequest> list) throws SQLException {
        String sql="INSERT INTO water_request(id,area_id,requested_litres,allocated_litres,unmet_litres,mode,created_at) VALUES(?,?,?,?,?,?,?)";
        try(PreparedStatement ps=c.prepareStatement(sql)) { for(WaterRequest r:list) { ps.setString(1,r.getId()); ps.setString(2,r.getAreaId()); ps.setInt(3,r.getRequestedLitres()); ps.setInt(4,r.getAllocatedLitres()); ps.setInt(5,r.getUnmetLitres()); ps.setString(6,r.getMode()); ps.setString(7,r.getCreatedAt()); ps.addBatch(); } ps.executeBatch(); }
    }

    private void loadRequests(Connection c, List<WaterRequest> list) throws SQLException {
        list.clear();
        try(Statement st=c.createStatement(); ResultSet rs=st.executeQuery("SELECT id,area_id,requested_litres,allocated_litres,mode,created_at FROM water_request ORDER BY id")) {
            while(rs.next()) {
                String stamp=rs.getString("created_at"); LocalDateTime time=LocalDateTime.now();
                try { time=LocalDateTime.parse(stamp); } catch(Exception ignored) { try { time=LocalDateTime.of(java.time.LocalDate.now(),java.time.LocalTime.parse(stamp)); } catch(Exception ignoredAgain) {} }
                WaterRequest r=new WaterRequest(rs.getString("id"),rs.getString("area_id"),rs.getInt("requested_litres"),rs.getString("mode"),time);
                r.recordAllocation(rs.getInt("allocated_litres")); list.add(r);
            }
        }
    }

    private void saveMaintenance(Connection c, List<MaintenanceRequest> list) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO maintenance_request(id,pipeline_id,description,created_at,resolved) VALUES(?,?,?,?,?)")) {
            for(MaintenanceRequest m:list) { ps.setString(1,m.getId()); ps.setString(2,m.getPipelineId()); ps.setString(3,m.getDescription()); ps.setString(4,m.getCreatedAt()); ps.setBoolean(5,m.isResolved()); ps.addBatch(); } ps.executeBatch();
        }
    }

    private void loadMaintenance(Connection c, Queue<MaintenanceRequest> queue, List<MaintenanceRequest> history) throws SQLException {
        queue.clear(); history.clear();
        try(Statement st=c.createStatement(); ResultSet rs=st.executeQuery("SELECT id,pipeline_id,description,created_at,resolved FROM maintenance_request ORDER BY id")) {
            while(rs.next()) { MaintenanceRequest m=new MaintenanceRequest(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getBoolean(5)); history.add(m); if(!m.isResolved()) queue.add(m); }
        }
    }

    private void saveTextHistory(Connection c, String table, List<String> values) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO " + table + "(entry_text) VALUES(?)")) { for(String value:values) { ps.setString(1,value); ps.addBatch(); } ps.executeBatch(); }
    }

    private void loadTextHistory(Connection c, String table, String column, List<String> target) throws SQLException {
        target.clear();
        try(Statement st=c.createStatement(); ResultSet rs=st.executeQuery("SELECT " + column + " FROM " + table + " ORDER BY entry_id")) { while(rs.next()) target.add(rs.getString(1)); }
    }

    private String safeMessage(Exception ex) {
        String message=ex.getMessage();
        if(message==null) message=ex.getClass().getSimpleName();
        return message.length()>180 ? message.substring(0,180) : message;
    }
}
