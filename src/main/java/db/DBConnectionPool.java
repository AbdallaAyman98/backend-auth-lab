package db;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DBConnectionPool {

    private static HikariDataSource dataSource;

    private DBConnectionPool() {}

    public static DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = buildDataSource();
        }
        return dataSource;
    }

    private static HikariDataSource buildDataSource() {
        return new HikariDataSource(HikariCPProperties.load());
    }

    private static void validateEnvVars() {
        String[] required = {"DB_URL", "DB_USERNAME", "DB_PASSWORD"};

        for (String var : required) {
            if (System.getenv(var) == null || System.getenv(var).isBlank()) {
                throw new IllegalStateException(
                        "Missing required environment variable: " + var
                );
            }
        }
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Connection pool closed");
        }
    }
}