package com.nach.util;

/**
 * Application constants for NACH Reprocessing System
 * Database and file upload configurations are handled by ApplicationConfig
 * This class contains only business logic constants and validation patterns
 */
public final class Constants {
    
    // ===== TRANSACTION STATUS CONSTANTS =====
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_STUCK = "STUCK";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REPROCESSED = "REPROCESSED";
    public static final String STATUS_PARSE_ERROR = "PARSE_ERROR";
    
    // Array of all valid statuses for validation
    public static final String[] ALL_STATUS_VALUES = {
        STATUS_SUCCESS, STATUS_ERROR, STATUS_STUCK, 
        STATUS_FAILED, STATUS_REPROCESSED, STATUS_PARSE_ERROR
    };
    
    // ===== FILE UPLOAD STATUS CONSTANTS =====
    public static final String FILE_STATUS_UPLOADED = "UPLOADED";
    public static final String FILE_STATUS_PROCESSING = "PROCESSING";
    public static final String FILE_STATUS_COMPLETED = "COMPLETED";
    public static final String FILE_STATUS_FAILED = "FAILED";
    
    // ===== NACH FILE TYPES =====
    public static final String FILE_TYPE_DR = "DR"; // Debit
    public static final String FILE_TYPE_CR = "CR"; // Credit
    
    // Array of valid file types
    public static final String[] ALL_FILE_TYPES = {FILE_TYPE_DR, FILE_TYPE_CR};
    
    // ===== ERROR CODES (Business Logic) =====
    public static final String ERROR_INSUFFICIENT_BALANCE = "E001";
    public static final String ERROR_TECHNICAL_ISSUE = "E002";
    public static final String ERROR_INVALID_MANDATE = "E003";
    public static final String ERROR_ACCOUNT_CLOSED = "E004";
    public static final String ERROR_INVALID_AMOUNT = "E005";
    public static final String ERROR_DUPLICATE_TRANSACTION = "E006";
    public static final String ERROR_SYSTEM_TIMEOUT = "E007";
    public static final String ERROR_NETWORK_ERROR = "E008";
    public static final String ERROR_VALIDATION_FAILED = "E009";
    public static final String ERROR_FILE_FORMAT_ERROR = "E010";
    public static final String ERROR_MANDATE_EXPIRED = "E011";
    public static final String ERROR_BANK_HOLIDAY = "E012";
    
    // ===== NACH FILE PARSING CONSTANTS =====
    public static final String FIELD_SEPARATOR = "\\|"; // Pipe separator for parsing
    public static final String FIELD_SEPARATOR_LITERAL = "|"; // Pipe separator for splitting
    public static final int MIN_FIELDS_COUNT = 4;
    public static final int MAX_FIELDS_COUNT = 10;
    
    // Field indexes in NACH file format
    public static final int TXN_REF_NO_INDEX = 0;
    public static final int MANDATE_ID_INDEX = 1;
    public static final int ACCOUNT_NO_INDEX = 2;
    public static final int AMOUNT_INDEX = 3;
    public static final int CUSTOMER_NAME_INDEX = 4;
    public static final int SPONSOR_BANK_INDEX = 5;
    public static final int DESTINATION_BANK_INDEX = 6;
    public static final int TRANSACTION_DATE_INDEX = 7;
    public static final int PURPOSE_CODE_INDEX = 8;
    
    // ===== VALIDATION PATTERNS =====
    public static final String ACCOUNT_NUMBER_PATTERN = "^[0-9]{10,20}$";
    public static final String MANDATE_ID_PATTERN = "^[A-Z0-9]{15,50}$";
    public static final String TXN_REF_PATTERN = "^[A-Z0-9]{10,50}$";
    public static final String AMOUNT_PATTERN = "^[0-9]+(\\.[0-9]{1,2})?$";
    public static final String BATCH_NO_PATTERN = "^[A-Z0-9]{2,20}$";
    public static final String ERROR_CODE_PATTERN = "^E[0-9]{3}$";
    
    // Email validation pattern
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    // Indian mobile number pattern
    public static final String MOBILE_PATTERN = "^[6-9][0-9]{9}$";
    
    // ===== NACH BUSINESS RULES =====
    public static final int MIN_TRANSACTION_AMOUNT = 1; // Minimum amount in rupees
    public static final int MAX_TRANSACTION_AMOUNT = 1000000; // Maximum amount in rupees (10 lakhs)
    public static final int MAX_RETRY_ATTEMPTS = 5;
    public static final int DEFAULT_BATCH_SIZE = 1000;
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    
    // ===== HTTP RESPONSE MESSAGES =====
    public static final String MSG_FILE_UPLOADED_SUCCESS = "File uploaded and processed successfully";
    public static final String MSG_FILE_UPLOAD_FAILED = "Failed to upload and process file";
    public static final String MSG_REPROCESS_SUCCESS = "Transactions reprocessed successfully";
    public static final String MSG_REPROCESS_FAILED = "Failed to reprocess transactions";
    public static final String MSG_NO_TRANSACTIONS_SELECTED = "No transactions selected for reprocessing";
    public static final String MSG_INVALID_FILE_FORMAT = "Invalid file format. Please upload a .txt file";
    public static final String MSG_FILE_SIZE_EXCEEDED = "File size exceeds maximum limit";
    public static final String MSG_INVALID_REQUEST = "Invalid request parameters";
    public static final String MSG_DATABASE_ERROR = "Database operation failed";
    public static final String MSG_UNAUTHORIZED_ACCESS = "Unauthorized access";
    public static final String MSG_TRANSACTION_NOT_FOUND = "Transaction not found";
    public static final String MSG_DUPLICATE_TRANSACTION = "Duplicate transaction reference number";
    
    // ===== JSON RESPONSE KEYS =====
    public static final String JSON_KEY_SUCCESS = "success";
    public static final String JSON_KEY_MESSAGE = "message";
    public static final String JSON_KEY_DATA = "data";
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_ERROR_CODE = "errorCode";
    public static final String JSON_KEY_TIMESTAMP = "timestamp";
    public static final String JSON_KEY_TRANSACTION_COUNT = "transactionCount";
    public static final String JSON_KEY_SUCCESS_COUNT = "successCount";
    public static final String JSON_KEY_ERROR_COUNT = "errorCount";
    public static final String JSON_KEY_TOTAL_COUNT = "totalCount";
    
    // ===== HTTP HEADERS =====
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    public static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
    public static final String HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
    
    // ===== MIME TYPES =====
    public static final String MIME_TYPE_JSON = "application/json";
    public static final String MIME_TYPE_TEXT = "text/plain";
    public static final String MIME_TYPE_HTML = "text/html";
    public static final String MIME_TYPE_XML = "application/xml";
    public static final String MIME_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String MIME_TYPE_MULTIPART = "multipart/form-data";
    
    // ===== DATE FORMATS =====
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_FORMAT_SIMPLE = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DISPLAY = "dd-MM-yyyy";
    public static final String DATETIME_FORMAT_DISPLAY = "dd-MM-yyyy HH:mm:ss";
    public static final String FILENAME_DATE_FORMAT = "ddMMyyyy";
    public static final String LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    
    // ===== DATABASE TABLE NAMES =====
    public static final String TABLE_NACH_TRANSACTIONS = "NACH_TRANSACTIONS";
    public static final String TABLE_NACH_FILE_UPLOADS = "NACH_FILE_UPLOADS";
    public static final String TABLE_NACH_REPROCESS_AUDIT = "NACH_REPROCESS_AUDIT";
    public static final String TABLE_NACH_ERROR_CONFIG = "NACH_ERROR_CONFIG";
    
    // ===== DATABASE COLUMN NAMES =====
    public static final String COL_ID = "ID";
    public static final String COL_TXN_REF_NO = "TXN_REF_NO";
    public static final String COL_MANDATE_ID = "MANDATE_ID";
    public static final String COL_ACCOUNT_NO = "ACCOUNT_NO";
    public static final String COL_AMOUNT = "AMOUNT";
    public static final String COL_STATUS = "STATUS";
    public static final String COL_ERROR_CODE = "ERROR_CODE";
    public static final String COL_ERROR_DESC = "ERROR_DESC";
    public static final String COL_PROCESSED_DATE = "PROCESSED_DATE";
    public static final String COL_FILE_NAME = "FILE_NAME";
    public static final String COL_BATCH_NO = "BATCH_NO";
    public static final String COL_FILE_TYPE = "FILE_TYPE";
    
    // ===== NACH FILE NAME PATTERNS =====
    public static final String NACH_DR_FILE_PREFIX = "ACH-DR-";
    public static final String NACH_CR_FILE_PREFIX = "ACH-CR-";
    public static final String NACH_FILE_EXTENSION = ".txt";
    public static final String NACH_FILE_PATTERN = "^(ACH-DR-|ACH-CR-)[A-Z0-9-]+\\.txt$";
    
    // ===== LOGGING CONSTANTS =====
    public static final String LOG_CATEGORY_DATABASE = "DATABASE";
    public static final String LOG_CATEGORY_FILE_UPLOAD = "FILE_UPLOAD";
    public static final String LOG_CATEGORY_REPROCESSING = "REPROCESSING";
    public static final String LOG_CATEGORY_VALIDATION = "VALIDATION";
    public static final String LOG_CATEGORY_SECURITY = "SECURITY";
    
    // ===== CACHE KEYS =====
    public static final String CACHE_ERROR_CONFIG = "errorConfigCache";
    public static final String CACHE_APPLICATION_CONFIG = "applicationConfig";
    public static final String CACHE_DATABASE_STATUS = "databaseStatus";
    public static final String CACHE_USER_SESSION = "userSession";
    
    // ===== SERVLET CONTEXT ATTRIBUTES =====
    public static final String CONTEXT_APP_CONFIG = "applicationConfig";
    public static final String CONTEXT_DB_STATUS = "databaseStatus";
    public static final String CONTEXT_ERROR_CONFIG_CACHE = "errorConfigCache";
    public static final String CONTEXT_STARTUP_TIME = "startupTime";
    
    // ===== REQUEST PARAMETERS =====
    public static final String PARAM_FILE = "file";
    public static final String PARAM_TRANSACTION_IDS = "transactionIds";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_DATE_FROM = "dateFrom";
    public static final String PARAM_DATE_TO = "dateTo";
    public static final String PARAM_SEARCH_TERM = "searchTerm";
    public static final String PARAM_PAGE_SIZE = "pageSize";
    public static final String PARAM_PAGE_NUMBER = "pageNumber";
    
    // ===== SYSTEM PROPERTIES =====
    public static final String SYSTEM_PROP_NACH_ENV = "nach.environment";
    public static final String SYSTEM_PROP_NACH_CONFIG = "nach.config.file";
    public static final String SYSTEM_PROP_NACH_LOG_LEVEL = "nach.log.level";
    
    // ===== ENVIRONMENT VARIABLES =====
    public static final String ENV_NACH_DB_URL = "NACH_DB_URL";
    public static final String ENV_NACH_DB_USERNAME = "NACH_DB_USERNAME";
    public static final String ENV_NACH_DB_PASSWORD = "NACH_DB_PASSWORD";
    public static final String ENV_NACH_UPLOAD_DIR = "NACH_UPLOAD_DIR";
    
    // ===== TRANSACTION PROCESSING CONSTANTS =====
    public static final int PROCESSING_BATCH_SIZE = 100;
    public static final int PROCESSING_TIMEOUT_SECONDS = 300;
    public static final int CLEANUP_INTERVAL_HOURS = 24;
    public static final int AUDIT_RETENTION_DAYS = 90;
    
    // ===== REPROCESSING CONSTANTS =====
    public static final String REPROCESS_REASON_MANUAL = "Manual reprocessing";
    public static final String REPROCESS_REASON_SCHEDULED = "Scheduled reprocessing";
    public static final String REPROCESS_REASON_AUTO_RETRY = "Automatic retry";
    public static final String REPROCESS_USER_SYSTEM = "SYSTEM";
    public static final String REPROCESS_USER_ADMIN = "ADMIN";
    
    // ===== UTILITY METHODS FOR VALIDATION =====
    
    /**
     * Check if transaction status is valid
     * @param status Status to validate
     * @return true if valid
     */
    public static boolean isValidStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        for (String validStatus : ALL_STATUS_VALUES) {
            if (validStatus.equals(status.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if file type is valid
     * @param fileType File type to validate
     * @return true if valid
     */
    public static boolean isValidFileType(String fileType) {
        if (fileType == null || fileType.trim().isEmpty()) {
            return false;
        }
        for (String validType : ALL_FILE_TYPES) {
            if (validType.equals(fileType.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if error code format is valid
     * @param errorCode Error code to validate
     * @return true if valid format
     */
    public static boolean isValidErrorCodeFormat(String errorCode) {
        return errorCode != null && errorCode.matches(ERROR_CODE_PATTERN);
    }
    
    /**
     * Check if account number format is valid
     * @param accountNo Account number to validate
     * @return true if valid format
     */
    public static boolean isValidAccountNumber(String accountNo) {
        return accountNo != null && accountNo.matches(ACCOUNT_NUMBER_PATTERN);
    }
    
    /**
     * Check if amount format is valid
     * @param amount Amount to validate
     * @return true if valid format
     */
    public static boolean isValidAmountFormat(String amount) {
        return amount != null && amount.matches(AMOUNT_PATTERN);
    }
    
    /**
     * Check if transaction reference number format is valid
     * @param txnRefNo Transaction reference number to validate
     * @return true if valid format
     */
    public static boolean isValidTxnRefNo(String txnRefNo) {
        return txnRefNo != null && txnRefNo.matches(TXN_REF_PATTERN);
    }
    
    /**
     * Check if mandate ID format is valid
     * @param mandateId Mandate ID to validate
     * @return true if valid format
     */
    public static boolean isValidMandateId(String mandateId) {
        return mandateId != null && mandateId.matches(MANDATE_ID_PATTERN);
    }
    
    /**
     * Check if NACH file name format is valid
     * @param fileName File name to validate
     * @return true if valid format
     */
    public static boolean isValidNachFileName(String fileName) {
        return fileName != null && fileName.matches(NACH_FILE_PATTERN);
    }
    
    /**
     * Get file type from file name
     * @param fileName File name
     * @return File type (DR/CR) or null if cannot determine
     */
    public static String getFileTypeFromName(String fileName) {
        if (fileName == null) {
            return null;
        }
        
        if (fileName.startsWith(NACH_DR_FILE_PREFIX)) {
            return FILE_TYPE_DR;
        } else if (fileName.startsWith(NACH_CR_FILE_PREFIX)) {
            return FILE_TYPE_CR;
        }
        
        return null;
    }
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}