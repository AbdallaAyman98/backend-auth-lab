package db;

import com.zaxxer.hikari.HikariConfig;
import configs.DatabaseConfig;
import utilities.ConfigReaderUtil;

public class HikariCPProperties {

    public static HikariConfig load() {

        HikariConfig objHikariConfig = new HikariConfig();
        DatabaseConfig objDataBaseConfig = DatabaseConfig.getInstance();

        // ── connection ───────────────────────────────────────
        objHikariConfig.setJdbcUrl(objDataBaseConfig.getUrl());
        objHikariConfig.setUsername(objDataBaseConfig.getUsername());
        objHikariConfig.setPassword(objDataBaseConfig.getPassword());

        // ── database ─────────────────────────────
//        objHikariConfig.setDriverClassName(
//                utilities.ConfigReaderUtil.get("driverClassName", "")
//        );

        // ── pool sizing ──────────────────────────
        objHikariConfig.setMinimumIdle(
                ConfigReaderUtil.getInt("minimumIdle", 5)
        );

        objHikariConfig.setMaximumPoolSize(
                ConfigReaderUtil.getInt("maximumPoolSize", 20)
        );

        // ── timeouts ─────────────────────────────
        objHikariConfig.setConnectionTimeout(
                ConfigReaderUtil.getLong("connectionTimeout", 30000)
        );

        objHikariConfig.setIdleTimeout(
                ConfigReaderUtil.getLong("idleTimeout", 600000)
        );

        objHikariConfig.setMaxLifetime(
                ConfigReaderUtil.getLong("maxLifetime", 1800000)
        );

        objHikariConfig.setKeepaliveTime(
                ConfigReaderUtil.getLong("keepaliveTime", 300000)
        );

        // ── health ───────────────────────────────
        objHikariConfig.setConnectionTestQuery(
                ConfigReaderUtil.get("connectionTestQuery", "SELECT 1")
        );

        objHikariConfig.setLeakDetectionThreshold(
                ConfigReaderUtil.getLong("leakDetectionThreshold", 0)
        );

        // ── identity ─────────────────────────────
        objHikariConfig.setPoolName(
                ConfigReaderUtil.get("poolName", "AppPool")
        );

        return objHikariConfig;
    }
}