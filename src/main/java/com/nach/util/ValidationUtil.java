package com.nach.util;

import com.nach.model.NachTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for validating NACH transactions and input data
 * Updated to work with ApplicationConfig and new Constants
 */
public class ValidationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);
    
    // Compiled patterns for better performance
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(Constants.ACCOUNT_NUMBER_PATTERN);
    private static final Pattern MANDATE_ID_PATTERN = Pattern.compile(Constants.MANDATE_ID_PATTERN);
    private static final Pattern TXN_REF_PATTERN = Pattern.compile(Constants.TXN_REF_PATTERN);
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(Constants.AMOUNT_PATTERN);
    private static final Pattern BATCH_NO_PATTERN = Pattern.compile(Constants.BATCH_NO_PATTERN);
    private static final Pattern ERROR_CODE_PATTERN = Pattern.compile(Constants.ERROR_CODE_PATTERN);
    
    /**
     * Validate file extension using ApplicationConfig
     * @param fileName File name to validate
     * @param config Application configuration
     * @return true if file extension is valid
     */
    public static boolean isValidFileExtension(String fileName, ApplicationConfig config) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(fileName);
        return config.isExtensionAllowed(extension);
    }
    
    /**
     * Validate file size using ApplicationConfig
     * @param fileSize File size in bytes
     * @param config Application configuration
     * @return true if file size is within limits
     */
    public static boolean isValidFileSize(long fileSize, ApplicationConfig config) {
        return fileSize > 0 && fileSize <= config.getMaxUploadSize();
    }
    
    /**
     * Get file extension from file name
     * @param fileName File name
     * @return File extension without dot
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return "";
    }
    
    /**
     * Validate account number format
     * @param accountNo Account number to validate
     * @return true if account number is valid
     */
    public static boolean isValidAccountNumber(String accountNo) {
        if (accountNo == null || accountNo.trim().isEmpty()) {
            return false;
        }
        return ACCOUNT_NUMBER_PATTERN.matcher(accountNo.trim()).matches();
    }
    
    /**
     * Validate mandate ID format
     * @param mandateId Mandate ID to validate
     * @return true if mandate ID is valid
     */
    public static boolean isValidMandateId(String mandateId) {
        if (mandateId == null || mandateId.trim().isEmpty()) {
            return false;
        }
        return MANDATE_ID_PATTERN.matcher(mandateId.trim()).matches();
    }
    
    /**
     * Validate transaction reference number format
     * @param txnRefNo Transaction reference number to validate
     * @return true if transaction reference is valid
     */
    public static boolean isValidTxnRefNo(String txnRefNo) {
        if (txnRefNo == null || txnRefNo.trim().isEmpty()) {
            return false;
        }
        return TXN_REF_PATTERN.matcher(txnRefNo.trim()).matches();
    }
    
    /**
     * Validate batch number format
     * @param batchNo Batch number to validate
     * @return true if batch number is valid
     */
    public static boolean isValidBatchNo(String batchNo) {
        if (batchNo == null || batchNo.trim().isEmpty()) {
            return false;
        }
        return BATCH_NO_PATTERN.matcher(batchNo.trim()).matches();
    }
    
    /**
     * Validate error code format
     * @param errorCode Error code to validate
     * @return true if error code is valid
     */
    public static boolean isValidErrorCode(String errorCode) {
        if (errorCode == null || errorCode.trim().isEmpty()) {
            return true; // Error code can be null for successful transactions
        }
        return ERROR_CODE_PATTERN.matcher(errorCode.trim()).matches();
    }
    
    /**
     * Validate amount format and value
     * @param amountStr Amount string to validate
     * @return true if amount is valid
     */
    public static boolean isValidAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return false;
        }
        
        if (!AMOUNT_PATTERN.matcher(amountStr.trim()).matches()) {
            return false;
        }
        
        try {
            BigDecimal amount = new BigDecimal(amountStr.trim());
            return amount.compareTo(BigDecimal.valueOf(Constants.MIN_TRANSACTION_AMOUNT)) >= 0 && 
                   amount.compareTo(BigDecimal.valueOf(Constants.MAX_TRANSACTION_AMOUNT)) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate amount value (BigDecimal)
     * @param amount Amount to validate
     * @return true if amount is valid
     */
    public static boolean isValidAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        
        return amount.compareTo(BigDecimal.valueOf(Constants.MIN_TRANSACTION_AMOUNT)) >= 0 && 
               amount.compareTo(BigDecimal.valueOf(Constants.MAX_TRANSACTION_AMOUNT)) <= 0;
    }
    
    /**
     * Validate file type (DR or CR)
     * @param fileType File type to validate
     * @return true if file type is valid
     */
    public static boolean isValidFileType(String fileType) {
        return Constants.isValidFileType(fileType);
    }
    
    /**
     * Validate transaction status
     * @param status Status to validate
     * @return true if status is valid
     */
    public static boolean isValidStatus(String status) {
        return Constants.isValidStatus(status);
    }
    
    /**
     * Validate NACH file name format
     * @param fileName File name to validate
     * @return true if file name follows NACH format
     */
    public static boolean isValidNachFileName(String fileName) {
        return Constants.isValidNachFileName(fileName);
    }
    
    /**
     * Validate customer name (basic validation)
     * @param customerName Customer name to validate
     * @return true if customer name is valid
     */
    public static boolean isValidCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return false;
        }
        
        String name = customerName.trim();
        return name.length() >= 2 && name.length() <= 100 && 
               name.matches("^[a-zA-Z\\s.'-]+$");
    }
    
    /**
     * Validate email format
     * @param email Email to validate
     * @return true if email is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches(Constants.EMAIL_PATTERN);
    }
    
    /**
     * Validate mobile number (Indian format)
     * @param mobile Mobile number to validate
     * @return true if mobile number is valid
     */
    public static boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        return mobile.trim().matches(Constants.MOBILE_PATTERN);
    }
    
    /**
     * Validate complete NACH transaction
     * @param transaction Transaction to validate
     * @return ValidationResult with details
     */
    public static ValidationResult validateTransaction(NachTransaction transaction) {
        List<String> errors = new ArrayList<>();
        
        if (transaction == null) {
            errors.add("Transaction cannot be null");
            return new ValidationResult(false, errors);
        }
        
        // Validate transaction reference number
        if (!isValidTxnRefNo(transaction.getTxnRefNo())) {
            errors.add("Invalid transaction reference number: " + transaction.getTxnRefNo());
        }
        
        // Validate mandate ID
        if (!isValidMandateId(transaction.getMandateId())) {
            errors.add("Invalid mandate ID: " + transaction.getMandateId());
        }
        
        // Validate account number
        if (!isValidAccountNumber(transaction.getAccountNo())) {
            errors.add("Invalid account number: " + transaction.getAccountNo());
        }
        
        // Validate amount
        if (!isValidAmount(transaction.getAmount())) {
            errors.add("Invalid amount: must be between " + Constants.MIN_TRANSACTION_AMOUNT + 
                      " and " + Constants.MAX_TRANSACTION_AMOUNT);
        }
        
        // Validate file type
        if (!isValidFileType(transaction.getFileType())) {
            errors.add("Invalid file type: " + transaction.getFileType());
        }
        
        // Validate status
        if (!isValidStatus(transaction.getStatus())) {
            errors.add("Invalid status: " + transaction.getStatus());
        }
        
        // Validate file name
        if (transaction.getFileName() == null || transaction.getFileName().trim().isEmpty()) {
            errors.add("File name cannot be empty");
        } else if (!isValidNachFileName(transaction.getFileName())) {
            errors.add("Invalid NACH file name format: " + transaction.getFileName());
        }
        
        // Validate batch number
        if (transaction.getBatchNo() != null && !isValidBatchNo(transaction.getBatchNo())) {
            errors.add("Invalid batch number format: " + transaction.getBatchNo());
        }
        
        // Validate error code format
        if (!isValidErrorCode(transaction.getErrorCode())) {
            errors.add("Invalid error code format: " + transaction.getErrorCode());
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validate parsed line from NACH file
     * @param fields Array of fields from parsed line
     * @param lineNumber Line number for error reporting
     * @return ValidationResult with details
     */
    public static ValidationResult validateParsedLine(String[] fields, int lineNumber) {
        List<String> errors = new ArrayList<>();
        
        if (fields == null || fields.length < Constants.MIN_FIELDS_COUNT) {
            errors.add("Line " + lineNumber + ": Insufficient fields. Expected at least " + 
                      Constants.MIN_FIELDS_COUNT + ", found " + (fields != null ? fields.length : 0));
            return new ValidationResult(false, errors);
        }
        
        // Validate transaction reference
        if (Constants.TXN_REF_NO_INDEX < fields.length) {
            if (!isValidTxnRefNo(fields[Constants.TXN_REF_NO_INDEX])) {
                errors.add("Line " + lineNumber + ": Invalid transaction reference number");
            }
        }
        
        // Validate mandate ID
        if (Constants.MANDATE_ID_INDEX < fields.length) {
            if (!isValidMandateId(fields[Constants.MANDATE_ID_INDEX])) {
                errors.add("Line " + lineNumber + ": Invalid mandate ID");
            }
        }
        
        // Validate account number
        if (Constants.ACCOUNT_NO_INDEX < fields.length) {
            if (!isValidAccountNumber(fields[Constants.ACCOUNT_NO_INDEX])) {
                errors.add("Line " + lineNumber + ": Invalid account number");
            }
        }
        
        // Validate amount
        if (Constants.AMOUNT_INDEX < fields.length) {
            if (!isValidAmount(fields[Constants.AMOUNT_INDEX])) {
                errors.add("Line " + lineNumber + ": Invalid amount");
            }
        }
        
        // Validate customer name if present
        if (Constants.CUSTOMER_NAME_INDEX < fields.length) {
            if (!isValidCustomerName(fields[Constants.CUSTOMER_NAME_INDEX])) {
                errors.add("Line " + lineNumber + ": Invalid customer name");
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validate NACH file header
     * @param headerFields Array of header fields
     * @return ValidationResult with details
     */
    public static ValidationResult validateNachHeader(String[] headerFields) {
        List<String> errors = new ArrayList<>();
        
        if (headerFields == null || headerFields.length < Constants.MIN_FIELDS_COUNT) {
            errors.add("Invalid header: Expected at least " + Constants.MIN_FIELDS_COUNT + 
                      " fields, found " + (headerFields != null ? headerFields.length : 0));
            return new ValidationResult(false, errors);
        }
        
        // Check for expected header field names
        boolean hasValidHeaders = false;
        for (String field : headerFields) {
            String upperField = field.trim().toUpperCase();
            if (upperField.contains("TXN") || upperField.contains("MANDATE") || 
                upperField.contains("ACCOUNT") || upperField.contains("AMOUNT")) {
                hasValidHeaders = true;
                break;
            }
        }
        
        if (!hasValidHeaders) {
            errors.add("Header does not contain expected field names (TXN, MANDATE, ACCOUNT, AMOUNT)");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Check if transaction is eligible for reprocessing
     * @param transaction Transaction to check
     * @return true if transaction can be reprocessed
     */
    public static boolean isReprocessable(NachTransaction transaction) {
        if (transaction == null || transaction.getStatus() == null) {
            return false;
        }
        
        String status = transaction.getStatus().toUpperCase();
        return Constants.STATUS_ERROR.equals(status) ||
               Constants.STATUS_STUCK.equals(status) ||
               Constants.STATUS_FAILED.equals(status);
    }
    
    /**
     * Check if transaction is in terminal state (cannot be changed)
     * @param transaction Transaction to check
     * @return true if transaction is in terminal state
     */
    public static boolean isTerminalState(NachTransaction transaction) {
        if (transaction == null || transaction.getStatus() == null) {
            return false;
        }
        
        String status = transaction.getStatus().toUpperCase();
        return Constants.STATUS_SUCCESS.equals(status) ||
               Constants.STATUS_REPROCESSED.equals(status);
    }
    
    /**
     * Sanitize string input to prevent injection attacks
     * @param input Input string to sanitize
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                    .replaceAll("[<>\"'%;()&+\\\\]", "") // Remove potentially dangerous characters
                    .replaceAll("\\s+", " "); // Normalize whitespace
    }
    
    /**
     * Sanitize input for SQL queries (additional protection)
     * @param input Input string to sanitize
     * @return Sanitized string safe for SQL
     */
    public static String sanitizeSqlInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                    .replaceAll("['\"\\\\;--]", "") // Remove SQL injection characters
                    .replaceAll("(?i)(union|select|insert|update|delete|drop|create|alter)", "") // Remove SQL keywords
                    .replaceAll("\\s+", " ");
    }
    
    /**
     * Validate request parameters for API calls
     * @param transactionIds List of transaction IDs
     * @return ValidationResult with details
     */
    public static ValidationResult validateReprocessRequest(List<Long> transactionIds) {
        List<String> errors = new ArrayList<>();
        
        if (transactionIds == null || transactionIds.isEmpty()) {
            errors.add("Transaction IDs list cannot be empty");
            return new ValidationResult(false, errors);
        }
        
        if (transactionIds.size() > 1000) {
            errors.add("Cannot reprocess more than 1000 transactions at once");
        }
        
        for (Long id : transactionIds) {
            if (id == null || id <= 0) {
                errors.add("Invalid transaction ID: " + id);
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public String getErrorsAsString() {
            return String.join("; ", errors);
        }
        
        public String getErrorMessage() {
            return String.join(", ", errors);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public int getErrorCount() {
            return errors.size();
        }
        
        public String getFirstError() {
            return errors.isEmpty() ? null : errors.get(0);
        }
    }
}