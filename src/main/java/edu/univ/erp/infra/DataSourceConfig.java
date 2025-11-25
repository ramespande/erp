package edu.univ.erp.infra;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Configures and provides DataSource instances for Auth and ERP databases
 * using HikariCP connection pooling.
 */
public final class DataSourceConfig {

    private static DataSource authDataSource;
    private static DataSource erpDataSource;

    private DataSourceConfig() {
        // Prevent instantiation
    }

    /**
     * Returns the DataSource for the Auth database (erp_auth).
     * Creates it on first access.
     */
    public static synchronized DataSource getAuthDataSource() {
        if (authDataSource == null) {
            authDataSource = createAuthDataSource();
        }
        return authDataSource;
    }

    /**
     * Returns the DataSource for the ERP database (erp_data).
     * Creates it on first access.
     */
    public static synchronized DataSource getErpDataSource() {
        if (erpDataSource == null) {
            erpDataSource = createErpDataSource();
        }
        return erpDataSource;
    }

    /**
     * Creates HikariCP DataSource for Auth database.
     */
    private static DataSource createAuthDataSource() {
        HikariConfig config = new HikariConfig();
        
        // JDBC URL - read from environment or use default
        String url = System.getenv().getOrDefault(
            "ERP_AUTH_DB_URL",
            "jdbc:mysql://localhost:3306/erp_auth?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
        
        String username = System.getenv().getOrDefault("ERP_AUTH_DB_USER", "root");
        String password = System.getenv().getOrDefault("ERP_AUTH_DB_PASSWORD", "");
        
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        
        // Connection pool settings
        int poolSize = Integer.parseInt(System.getenv().getOrDefault("ERP_DB_POOL_SIZE", "8"));
        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        
        // Pool name for monitoring
        config.setPoolName("AuthDB-Pool");
        
        // Recommended settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        return new HikariDataSource(config);
    }

    /**
     * Creates HikariCP DataSource for ERP database.
     */
    private static DataSource createErpDataSource() {
        HikariConfig config = new HikariConfig();
        
        // JDBC URL - read from environment or use default
        String url = System.getenv().getOrDefault(
            "ERP_DATA_DB_URL",
            "jdbc:mysql://localhost:3306/erp_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
        
        String username = System.getenv().getOrDefault("ERP_DATA_DB_USER", "root");
        String password = System.getenv().getOrDefault("ERP_DATA_DB_PASSWORD", "");
        
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        
        // Connection pool settings
        int poolSize = Integer.parseInt(System.getenv().getOrDefault("ERP_DB_POOL_SIZE", "8"));
        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        
        // Pool name for monitoring
        config.setPoolName("ErpDB-Pool");
        
        // Recommended settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        return new HikariDataSource(config);
    }

    /**
     * Closes all connection pools. Call this on application shutdown.
     */
    public static synchronized void shutdown() {
        if (authDataSource instanceof HikariDataSource) {
            ((HikariDataSource) authDataSource).close();
        }
        if (erpDataSource instanceof HikariDataSource) {
            ((HikariDataSource) erpDataSource).close();
        }
        authDataSource = null;
        erpDataSource = null;
    }
}