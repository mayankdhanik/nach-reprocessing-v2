package com.nach.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.io.File;

/**
 * Application configuration management class
 * Handles all configuration properties for NACH Reprocessing System
 * Loads from application.properties with fallback defaults
 */
public class ApplicationConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    
    private final Properties properties;
    
    // Configuration property keys
    private static final String KEY_APP_NAME = "app.name";
    private static final String KEY_APP_VERSION = "app.version";
    private static final String KEY_APP_ENVIRONMENT = "app.environment";
    
    private static final String KEY_DB_URL = "db.url";
    private static final String KEY_DB_USERNAME = "db.username";
    private static final String KEY_DB_PASSWORD = "db.password";
    private static final String KEY_DB_DRIVER = "db.driver";
    private static final String KEY_DB_POOL_INITIAL_SIZE = "db.pool.initial.size";
    private static final String KEY_DB_POOL_MAX_SIZE = "db.pool.max.size";
    private static final String KEY_DB_POOL_MIN_IDLE = "db.pool.min.idle";
    private static final String KEY_DB_POOL_MAX_IDLE = "db.pool.max.idle";
    
    private static final String KEY_UPLOAD_MAX_SIZE = "upload.max.size";
    private static final String KEY_UPLOAD_TEMP_DIR = "upload.temp.dir";
    private static final String KEY_UPLOAD_ALLOWED_EXTENSIONS = "upload.allowed.extensions";
    
    private static final String KEY_LOG_LEVEL = "log.level";
    private static final String KEY_LOG_FILE = "log.file";
    
    private static final String KEY_CORS_ALLOWED_ORIGINS = "security.cors.allowed.origins";
    private static final String KEY_SESSION_TIMEOUT = "security.session.timeout";
    
    private static final String KEY_NACH_BATCH_SIZE = "nach.batch.size";
    private static final String KEY_NACH_RETRY_MAX_ATTEMPTS = "nach.retry.max.attempts";
    private static final String KEY_NACH_PROCESSING_TIMEOUT = "nach.processing.timeout";
    
    private static final String KEY_DEBUG_MODE = "debug.mode";
    private static final String KEY_CREATE_SAMPLE_DATA = "create.sample.data";
    
    /**
     * Constructor - loads configuration from properties
     * @param properties Properties object with configuration
     */
    public ApplicationConfig(Properties properties) {
        this.properties = properties != null ? properties : new Properties();
        validateConfiguration();
        logConfiguration();
    }
    
    /**
     * Validate critical configuration properties
     */
    private void validateConfiguration() {
        StringBuilder errors = new StringBuilder();
        
        // Validate database configuration
        if (getDatabaseUrl() == null || getDatabaseUrl().trim().isEmpty()) {
            errors.append("Database URL is required (db.url property)\n");
        }
        
        if (getDatabaseUsername() == null || getDatabaseUsername().trim().isEmpty()) {
            errors.append("Database username is required (db.username property)\n");
        }
        
        if (getDatabaseDriver() == null || getDatabaseDriver().trim().isEmpty()) {
            errors.append("Database driver is required (db.driver property)\n");
        }
        
        // Validate upload configuration
        if (getMaxUploadSize() <= 0) {
            errors.append("Maximum upload size must be positive (upload.max.size property)\n");
        }
        
        if (getUploadDirectory() == null || getUploadDirectory().trim().isEmpty()) {
            errors.append("Upload directory is required (upload.temp.dir property)\n");
        }
        
        if (errors.length() > 0) {
            logger.error("Configuration validation failed:\n{}", errors.toString());
            throw new RuntimeException("Invalid configuration:\n" + errors.toString());
        }
        
        logger.info("Configuration validation successful");
    }
    
    /**
     * Log configuration summary (without sensitive data)
     */
    private void logConfiguration() {
        if (logger.isInfoEnabled()) {
            logger.info("=== Application Configuration ===");
            logger.info("Application: {} v{}", getApplicationName(), getVersion());
            logger.info("Environment: {}", getEnvironment());
            logger.info("Database Driver: {}", getDatabaseDriver());
            logger.info("Database URL: {}", maskSensitiveUrl(getDatabaseUrl()));
            logger.info("Database Username: {}", getDatabaseUsername());
            logger.info("Upload Directory: {}", getUploadDirectory());
            logger.info("Max Upload Size: {} MB", getMaxUploadSize() / 1024 / 1024);
            logger.info("Debug Mode: {}", isDebugMode());
            logger.info("=================================");
        }
    }
    
    // ===== APPLICATION PROPERTIES =====
    
    /**
     * Get application name
     * @return Application name
     */
    public String getApplicationName() {
        return properties.getProperty(KEY_APP_NAME, "NACH Reprocessing System");
    }
    
    /**
     * Get application version
     * @return Application version
     */
    public String getVersion() {
        return properties.getProperty(KEY_APP_VERSION, "1.0.0");
    }
    
    /**
     * Get current environment
     * @return Environment (development, production, test, etc.)
     */
    public String getEnvironment() {
        return properties.getProperty(KEY_APP_ENVIRONMENT, "development");
    }
    
    /**
     * Check if running in development environment
     * @return true if development environment
     */
    public boolean isDevelopmentMode() {
        return "development".equalsIgnoreCase(getEnvironment());
    }
    
    /**
     * Check if running in production environment
     * @return true if production environment
     */
    public boolean isProductionMode() {
        return "production".equalsIgnoreCase(getEnvironment());
    }
    
    /**
     * Check if running in test environment
     * @return true if test environment
     */
    public boolean isTestMode() {
        return "test".equalsIgnoreCase(getEnvironment());
    }
    
    // ===== DATABASE PROPERTIES =====
    
    /**
     * Get database URL
     * @return Database connection URL
     */
    public String getDatabaseUrl() {
        return properties.getProperty(KEY_DB_URL);
    }
    
    /**
     * Get database username
     * @return Database username
     */
    public String getDatabaseUsername() {
        return properties.getProperty(KEY_DB_USERNAME);
    }
    
    /**
     * Get database password
     * @return Database password
     */
    public String getDatabasePassword() {
        return properties.getProperty(KEY_DB_PASSWORD, "");
    }
    
    /**
     * Get database driver class name
     * @return Database driver class name
     */
    public String getDatabaseDriver() {
        return properties.getProperty(KEY_DB_DRIVER);
    }
    
    /**
     * Get database connection pool initial size
     * @return Initial pool size
     */
    public int getDatabasePoolInitialSize() {
        return getIntProperty(KEY_DB_POOL_INITIAL_SIZE, 5);
    }
    
    /**
     * Get database connection pool maximum size
     * @return Maximum pool size
     */
    public int getDatabasePoolMaxSize() {
        return getIntProperty(KEY_DB_POOL_MAX_SIZE, 20);
    }
    
    /**
     * Get database connection pool minimum idle connections
     * @return Minimum idle connections
     */
    public int getDatabasePoolMinIdle() {
        return getIntProperty(KEY_DB_POOL_MIN_IDLE, 2);
    }
    
    /**
     * Get database connection pool maximum idle connections
     * @return Maximum idle connections
     */
    public int getDatabasePoolMaxIdle() {
        return getIntProperty(KEY_DB_POOL_MAX_IDLE, 10);
    }
    
    /**
     * Check if database is Oracle
     * @return true if Oracle database
     */
    public boolean isOracleDatabase() {
        String url = getDatabaseUrl();
        String driver = getDatabaseDriver();
        return (url != null && url.contains("oracle")) || 
               (driver != null && driver.contains("oracle"));
    }
    
    /**
     * Check if database is MySQL
     * @return true if MySQL database
     */
    public boolean isMySqlDatabase() {
        String url = getDatabaseUrl();
        String driver = getDatabaseDriver();
        return (url != null && url.contains("mysql")) || 
               (driver != null && driver.contains("mysql"));
    }
    
    /**
     * Check if database is PostgreSQL
     * @return true if PostgreSQL database
     */
    public boolean isPostgreSqlDatabase() {
        String url = getDatabaseUrl();
        String driver = getDatabaseDriver();
        return (url != null && url.contains("postgresql")) || 
               (driver != null && driver.contains("postgresql"));
    }
    
    /**
     * Get database type as string
     * @return Database type name
     */
    public String getDatabaseType() {
        if (isOracleDatabase()) return "Oracle";
        if (isMySqlDatabase()) return "MySQL";
        if (isPostgreSqlDatabase()) return "PostgreSQL";
        return "Unknown";
    }
    
    // ===== FILE UPLOAD PROPERTIES =====
    
    /**
     * Get maximum file upload size in bytes
     * @return Maximum upload size
     */
    public long getMaxUploadSize() {
        return getLongProperty(KEY_UPLOAD_MAX_SIZE, 10485760L); // 10MB default
    }
    
    /**
     * Get maximum file upload size in MB
     * @return Maximum upload size in MB
     */
    public long getMaxUploadSizeMB() {
        return getMaxUploadSize() / 1024 / 1024;
    }
    
    /**
     * Get upload temporary directory
     * @return Upload directory path
     */
    public String getUploadDirectory() {
        String dir = properties.getProperty(KEY_UPLOAD_TEMP_DIR, System.getProperty("java.io.tmpdir"));
        
        // Ensure directory exists
        File uploadDir = new File(dir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            logger.info("Created upload directory: {}", dir);
        }
        
        return dir;
    }
    
    /**
     * Get allowed file extensions for upload
     * @return Comma-separated list of allowed extensions
     */
    public String getAllowedExtensions() {
        return properties.getProperty(KEY_UPLOAD_ALLOWED_EXTENSIONS, "txt");
    }
    
    /**
     * Get allowed file extensions as array
     * @return Array of allowed extensions
     */
    public String[] getAllowedExtensionsArray() {
        String extensions = getAllowedExtensions();
        return extensions.split(",");
    }
    
    /**
     * Check if file extension is allowed
     * @param extension File extension to check
     * @return true if extension is allowed
     */
    public boolean isExtensionAllowed(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return false;
        }
        
        String[] allowed = getAllowedExtensionsArray();
        String ext = extension.toLowerCase().replace(".", "");
        
        for (String allowedExt : allowed) {
            if (allowedExt.trim().toLowerCase().equals(ext)) {
                return true;
            }
        }
        
        return false;
    }
    
    // ===== LOGGING PROPERTIES =====
    
    /**
     * Get logging level
     * @return Log level (DEBUG, INFO, WARN, ERROR)
     */
    public String getLogLevel() {
        return properties.getProperty(KEY_LOG_LEVEL, "INFO");
    }
    
    /**
     * Get log file path
     * @return Log file path
     */
    public String getLogFile() {
        return properties.getProperty(KEY_LOG_FILE, "logs/nach-reprocessing.log");
    }
    
    // ===== SECURITY PROPERTIES =====
    
    /**
     * Get CORS allowed origins
     * @return Comma-separated list of allowed origins
     */
    public String getCorsAllowedOrigins() {
        return properties.getProperty(KEY_CORS_ALLOWED_ORIGINS, 
            "http://localhost:3000,http://localhost:8080");
    }
    
    /**
     * Get CORS allowed origins as array
     * @return Array of allowed origins
     */
    public String[] getCorsAllowedOriginsArray() {
        String origins = getCorsAllowedOrigins();
        return origins.split(",");
    }
    
    /**
     * Check if origin is allowed for CORS
     * @param origin Origin to check
     * @return true if origin is allowed
     */
    public boolean isOriginAllowed(String origin) {
        if (origin == null || origin.trim().isEmpty()) {
            return false;
        }
        
        String[] allowed = getCorsAllowedOriginsArray();
        for (String allowedOrigin : allowed) {
            if (allowedOrigin.trim().equals(origin.trim())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get session timeout in minutes
     * @return Session timeout
     */
    public int getSessionTimeout() {
        return getIntProperty(KEY_SESSION_TIMEOUT, 30);
    }
    
    // ===== NACH PROCESSING PROPERTIES =====
    
    /**
     * Get NACH batch processing size
     * @return Batch size for processing
     */
    public int getNachBatchSize() {
        return getIntProperty(KEY_NACH_BATCH_SIZE, 1000);
    }
    
    /**
     * Get maximum retry attempts for failed transactions
     * @return Maximum retry attempts
     */
    public int getNachRetryMaxAttempts() {
        return getIntProperty(KEY_NACH_RETRY_MAX_ATTEMPTS, 3);
    }
    
    /**
     * Get processing timeout in seconds
     * @return Processing timeout
     */
    public int getNachProcessingTimeout() {
        return getIntProperty(KEY_NACH_PROCESSING_TIMEOUT, 300);
    }
    
    // ===== DEBUG PROPERTIES =====
    
    /**
     * Check if debug mode is enabled
     * @return true if debug mode
     */
    public boolean isDebugMode() {
        return getBooleanProperty(KEY_DEBUG_MODE, isDevelopmentMode());
    }
    
    /**
     * Check if sample data creation is enabled
     * @return true if sample data should be created
     */
    public boolean isCreateSampleData() {
        return getBooleanProperty(KEY_CREATE_SAMPLE_DATA, isDevelopmentMode());
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Get integer property with default value
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Integer value
     */
    private int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property {}: {}. Using default: {}", 
                key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get long property with default value
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Long value
     */
    private long getLongProperty(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid long value for property {}: {}. Using default: {}", 
                key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get boolean property with default value
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Boolean value
     */
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value.trim());
    }
    
    /**
     * Get property with default value
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get property without default
     * @param key Property key
     * @return Property value or null
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Check if property exists
     * @param key Property key
     * @return true if property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Get all properties
     * @return Properties object
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
    
    /**
     * Mask sensitive information in URL for logging
     * @param url URL to mask
     * @return Masked URL
     */
    private String maskSensitiveUrl(String url) {
        if (url == null) {
            return "null";
        }
        
        // Hide password from URL if present
        if (url.contains("password=")) {
            return url.replaceAll("password=[^&;]*", "password=***");
        }
        
        // Hide user info from URL
        if (url.contains("://") && url.contains("@")) {
            return url.replaceAll("://[^@]*@", "://***:***@");
        }
        
        return url;
    }
    
    /**
     * Get configuration summary for display
     * @return Configuration summary string
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Application: ").append(getApplicationName()).append(" v").append(getVersion()).append("\n");
        summary.append("Environment: ").append(getEnvironment()).append("\n");
        summary.append("Database: ").append(getDatabaseType()).append("\n");
        summary.append("Upload Directory: ").append(getUploadDirectory()).append("\n");
        summary.append("Max Upload Size: ").append(getMaxUploadSizeMB()).append(" MB\n");
        summary.append("Debug Mode: ").append(isDebugMode()).append("\n");
        summary.append("Batch Size: ").append(getNachBatchSize()).append("\n");
        summary.append("Session Timeout: ").append(getSessionTimeout()).append(" minutes\n");
        return summary.toString();
    }
    
    /**
     * ToString method for debugging
     * @return String representation (without sensitive data)
     */
    @Override
    public String toString() {
        return String.format("ApplicationConfig{app=%s, version=%s, env=%s, db=%s, debug=%s}", 
            getApplicationName(), getVersion(), getEnvironment(), getDatabaseType(), isDebugMode());
    }
}