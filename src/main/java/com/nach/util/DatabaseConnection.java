package com.nach.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;
import java.io.InputStream;

/**
 * Database connection utility - CONNECTIVITY ONLY
 * NO table creation - all database setup must be done separately
 * Works with any database (Oracle, H2, MySQL, PostgreSQL, etc.)
 */
public class DatabaseConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    
    // Database configuration loaded from properties
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbDriver;
    private static String environment;
    
    // Required tables that must exist in database
    private static final String[] REQUIRED_TABLES = {
        "NACH_TRANSACTIONS",
        "NACH_FILE_UPLOADS", 
        "NACH_REPROCESS_AUDIT",
        "NACH_ERROR_CONFIG"
    };
    
    // Static initialization - load configuration only
    static {
        loadDatabaseConfiguration();
        initializeDriver();
    }
    
    /**
     * Load database configuration from application.properties
     */
    private static void loadDatabaseConfiguration() {
        try {
            Properties props = new Properties();
            InputStream configStream = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties");
            
            if (configStream != null) {
                props.load(configStream);
                configStream.close();
                logger.info("Database configuration loaded from application.properties");
            } else {
                // Use system properties or environment variables as fallback
                props = loadFromSystemProperties();
                logger.info("Database configuration loaded from system properties");
            }
            
            // Load database connection properties
            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");
            dbDriver = props.getProperty("db.driver");
            environment = props.getProperty("app.environment", "development");
            
            // Validate required properties
            validateConfiguration();
            
            logger.info("Database Configuration:");
            logger.info("- URL: {}", maskUrl(dbUrl));
            logger.info("- Username: {}", dbUsername);
            logger.info("- Driver: {}", dbDriver);
            logger.info("- Environment: {}", environment);
            
        } catch (Exception e) {
            logger.error("Failed to load database configuration", e);
            throw new RuntimeException("Database configuration loading failed", e);
        }
    }
    
    /**
     * Load configuration from system properties/environment variables
     */
    private static Properties loadFromSystemProperties() {
        Properties props = new Properties();
        
        // Try system properties first, then environment variables
        props.setProperty("db.url", 
            System.getProperty("db.url", System.getenv("DB_URL")));
        props.setProperty("db.username", 
            System.getProperty("db.username", System.getenv("DB_USERNAME")));
        props.setProperty("db.password", 
            System.getProperty("db.password", System.getenv("DB_PASSWORD")));
        props.setProperty("db.driver", 
            System.getProperty("db.driver", System.getenv("DB_DRIVER")));
        props.setProperty("app.environment", 
            System.getProperty("app.environment", System.getenv("APP_ENVIRONMENT")));
        
        return props;
    }
    
    /**
     * Validate that all required configuration is present
     */
    private static void validateConfiguration() {
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            throw new RuntimeException("Database URL is required (db.url property)");
        }
        if (dbUsername == null) {
            throw new RuntimeException("Database username is required (db.username property)");
        }
        if (dbDriver == null || dbDriver.trim().isEmpty()) {
            throw new RuntimeException("Database driver is required (db.driver property)");
        }
        
        logger.info("Database configuration validation successful");
    }
    
    /**
     * Initialize database driver
     */
    private static void initializeDriver() {
        try {
            Class.forName(dbDriver);
            logger.info("Database driver loaded successfully: {}", dbDriver);
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load database driver: {}", dbDriver, e);
            throw new RuntimeException("Database driver not found: " + dbDriver, e);
        }
    }
    
    /**
     * Get database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Database connection established successfully");
            }
            
            return conn;
            
        } catch (SQLException e) {
            logger.error("Failed to establish database connection", e);
            logger.error("URL: {}", maskUrl(dbUrl));
            logger.error("Username: {}", dbUsername);
            throw new SQLException("Database connection failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Test database connectivity
     * @return true if connection is successful
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            // Test with a simple query based on database type
            Statement stmt = conn.createStatement();
            ResultSet rs;
            
            if (dbUrl.contains("oracle")) {
                rs = stmt.executeQuery("SELECT 1 FROM DUAL");
            } else if (dbUrl.contains("mysql")) {
                rs = stmt.executeQuery("SELECT 1");
            } else if (dbUrl.contains("postgresql")) {
                rs = stmt.executeQuery("SELECT 1");
            } else if (dbUrl.contains("h2")) {
                rs = stmt.executeQuery("SELECT 1");
            } else {
                // Generic test query
                rs = stmt.executeQuery("SELECT 1");
            }
            
            boolean success = rs.next();
            rs.close();
            stmt.close();
            
            if (success) {
                logger.info("Database connection test SUCCESSFUL");
            } else {
                logger.error("Database connection test FAILED - no result returned");
            }
            
            return success;
            
        } catch (SQLException e) {
            logger.error("Database connection test FAILED", e);
            return false;
        }
    }
    
    /**
     * Validate that all required tables exist in the database
     * This method checks if the database schema is properly set up
     * @return true if all required tables exist
     */
    public static boolean validateRequiredTables() {
        try (Connection conn = getConnection()) {
            logger.info("Validating required database tables...");
            
            for (String tableName : REQUIRED_TABLES) {
                if (!tableExists(conn, tableName)) {
                    logger.error("Required table NOT FOUND: {}", tableName);
                    logger.error("Please run the database setup scripts before starting the application");
                    return false;
                }
                logger.debug("Table validated: {}", tableName);
            }
            
            logger.info("All required tables validated successfully");
            return true;
            
        } catch (SQLException e) {
            logger.error("Failed to validate required tables", e);
            return false;
        }
    }
    
    /**
     * Check if a specific table exists in the database
     * @param conn Database connection
     * @param tableName Name of the table to check
     * @return true if table exists
     * @throws SQLException if database error occurs
     */
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        // Try multiple approaches to check table existence
        
        // Method 1: Use database metadata
        try {
            ResultSet rs = conn.getMetaData().getTables(null, null, tableName.toUpperCase(), null);
            boolean exists = rs.next();
            rs.close();
            
            if (exists) {
                return true;
            }
            
            // Also try lowercase for case-sensitive databases
            rs = conn.getMetaData().getTables(null, null, tableName.toLowerCase(), null);
            exists = rs.next();
            rs.close();
            
            return exists;
            
        } catch (SQLException e) {
            logger.warn("Metadata approach failed for table {}, trying query approach", tableName);
        }
        
        // Method 2: Try a simple query on the table
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE 1=0");
            rs.close();
            stmt.close();
            return true; // If query succeeds, table exists
            
        } catch (SQLException e) {
            logger.debug("Table {} does not exist or is not accessible", tableName);
            return false;
        }
    }
    
    /**
     * Get database type based on URL
     * @return String describing the database type
     */
    public static String getDatabaseType() {
        if (dbUrl == null) {
            return "Unknown";
        }
        
        if (dbUrl.contains("oracle")) {
            return "Oracle";
        } else if (dbUrl.contains("mysql")) {
            return "MySQL";
        } else if (dbUrl.contains("postgresql")) {
            return "PostgreSQL";
        } else if (dbUrl.contains("h2")) {
            return "H2";
        } else if (dbUrl.contains("sqlserver")) {
            return "SQL Server";
        } else {
            return "Unknown";
        }
    }
    
    /**
     * Get current environment
     * @return Environment string (development, production, etc.)
     */
    public static String getEnvironment() {
        return environment != null ? environment : "unknown";
    }
    
    /**
     * Check if running in development environment
     * @return true if development environment
     */
    public static boolean isDevelopment() {
        return "development".equalsIgnoreCase(environment);
    }
    
    /**
     * Check if running in production environment
     * @return true if production environment
     */
    public static boolean isProduction() {
        return "production".equalsIgnoreCase(environment);
    }
    
    /**
     * Get database connection information (for logging/debugging)
     * @return String with connection info (passwords masked)
     */
    public static String getConnectionInfo() {
        return String.format("Database: %s, URL: %s, Username: %s, Environment: %s", 
            getDatabaseType(), maskUrl(dbUrl), dbUsername, getEnvironment());
    }
    
    /**
     * Mask database URL for logging (hide sensitive information)
     * @param url Database URL
     * @return Masked URL string
     */
    private static String maskUrl(String url) {
        if (url == null) {
            return "null";
        }
        
        // Hide password from URL if present
        if (url.contains("password=")) {
            return url.replaceAll("password=[^&;]", "password=**");
        }
        
        return url;
    }
    
    /**
     * Close database connection safely
     * @param conn Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                if (logger.isDebugEnabled()) {
                    logger.debug("Database connection closed successfully");
                }
            } catch (SQLException e) {
                logger.error("Failed to close database connection", e);
            }
        }
    }
    
    /**
     * Close database resources safely
     * @param conn Connection to close
     * @param stmt Statement to close
     * @param rs ResultSet to close
     */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Failed to close ResultSet", e);
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Failed to close Statement", e);
            }
        }
        
        closeConnection(conn);
    }
    
    /**
     * Execute a simple health check query
     * @return true if database is healthy and responsive
     */
    public static boolean isHealthy() {
        try {
            return testConnection() && validateRequiredTables();
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            return false;
        }
    }
}