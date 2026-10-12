/** Optional live database smoke test. Configure db.properties and add Connector/J to the runtime classpath first. */
public class DatabaseConnectionTest {
    public static void main(String[] args) {
        MySqlPersistence persistence = new MySqlPersistence();
        String result = persistence.testConnection();
        System.out.println(result);
        if (!result.startsWith("PASS:")) System.exit(1);
    }
}
