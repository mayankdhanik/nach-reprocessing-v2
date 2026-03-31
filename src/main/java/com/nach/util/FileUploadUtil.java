package com.nach.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Part;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for handling NACH file uploads
 * Works with ApplicationConfig for dynamic configuration
 */
public class FileUploadUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadUtil.class);
    
    /**
     * Validation result class for file upload validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join(", ", errors);
        }
    }
    
    /**
     * Extract file name from Part object
     * @param part File part from multipart request
     * @return File name or null if not found
     */
    public static String getFileName(Part part) {
        if (part == null) {
            return null;
        }
        
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null) {
            return null;
        }
        
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                String fileName = token.substring(token.indexOf('=') + 1).trim();
                return fileName.replaceAll("\"", "");
            }
        }
        return null;
    }
    
    /**
     * Validate uploaded file using ApplicationConfig
     * @param part File part from multipart request
     * @param config Application configuration
     * @return ValidationResult with details
     */
    public static ValidationResult validateUploadedFile(Part part, ApplicationConfig config) {
        List<String> errors = new ArrayList<>();
        
        if (part == null) {
            errors.add("No file uploaded");
            return new ValidationResult(false, errors);
        }
        
        String fileName = getFileName(part);
        if (fileName == null || fileName.trim().isEmpty()) {
            errors.add("File name is missing");
            return new ValidationResult(false, errors);
        }
        
        // Validate file extension using ApplicationConfig
        if (!isValidFileExtension(fileName, config)) {
            errors.add(Constants.MSG_INVALID_FILE_FORMAT);
        }
        
        // Validate file size using ApplicationConfig
        long fileSize = part.getSize();
        if (!isValidFileSize(fileSize, config)) {
            errors.add(Constants.MSG_FILE_SIZE_EXCEEDED + " (" + config.getMaxUploadSizeMB() + " MB)");
        }
        
        // Validate NACH file name pattern
        if (!Constants.isValidNachFileName(fileName)) {
            errors.add("Invalid NACH file name format. Expected pattern: ACH-DR-* or ACH-CR-*");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Check if file extension is valid
     * @param fileName File name to check
     * @param config Application configuration
     * @return true if extension is allowed
     */
    private static boolean isValidFileExtension(String fileName, ApplicationConfig config) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(fileName);
        return config.isExtensionAllowed(extension);
    }
    
    /**
     * Check if file size is valid
     * @param fileSize File size in bytes
     * @param config Application configuration
     * @return true if size is within limits
     **/
    private static boolean isValidFileSize(long fileSize, ApplicationConfig config) {
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
     * Read file content from Part object
     * @param part File part from multipart request
     * @return List of lines from the file
     * @throws IOException if file reading fails
     */
    public static List<String> readFileLines(Part part) throws IOException {
        List<String> lines = new ArrayList<>();
        
        try (InputStream inputStream = part.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines but keep track of line numbers
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
                
                // Prevent reading extremely large files
                if (lineNumber > 100000) { // Max 100k lines
                    logger.warn("File has too many lines, stopping at {}", lineNumber);
                    break;
                }
            }
        }
        
        logger.info("Read {} lines from uploaded file", lines.size());
        return lines;
    }
    
    /**
     * Read file content as string
     * @param part File part from multipart request
     * @return File content as string
     * @throws IOException if file reading fails
     */
    public static String readFileContent(Part part) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (InputStream inputStream = part.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        
        return content.toString();
    }
    
    /**
     * Save uploaded file to configured upload directory
     * @param part File part from multipart request
     * @param fileName Name to save the file as
     * @param config Application configuration
     * @return Path to saved file
     * @throws IOException if file saving fails
     */
    public static Path saveUploadedFile(Part part, String fileName, ApplicationConfig config) throws IOException {
        // Get upload directory from config
        Path uploadDir = Paths.get(config.getUploadDirectory());
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            logger.info("Created upload directory: {}", uploadDir);
        }
        
        // Generate unique file name to avoid conflicts
        String uniqueFileName = generateUniqueFileName(fileName);
        Path filePath = uploadDir.resolve(uniqueFileName);
        
        // Save file
        try (InputStream inputStream = part.getInputStream()) {
            Files.copy(inputStream, filePath);
        }
        
        logger.info("File saved successfully: {} (size: {})", 
                   filePath, getHumanReadableFileSize(part.getSize()));
        return filePath;
    }
    
    /**
     * Generate unique file name by appending timestamp
     * @param originalFileName Original file name
     * @return Unique file name
     */
    public static String generateUniqueFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            return "nach_file_" + System.currentTimeMillis() + ".txt";
        }
        
        String name = originalFileName;
        String extension = "";
        
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            name = originalFileName.substring(0, lastDotIndex);
            extension = originalFileName.substring(lastDotIndex);
        }
        
        return name + "_" + System.currentTimeMillis() + extension;
    }
    
    /**
     * Extract file type from file name using Constants utility
     * @param fileName File name to analyze
     * @return File type (DR or CR) or null if cannot determine
     */
    public static String extractFileType(String fileName) {
        return Constants.getFileTypeFromName(fileName);
    }
    
    /**
     * Extract batch number from NACH file name
     * @param fileName File name to analyze
     * @return Batch number or generated one if not found
     */
    public static String extractBatchNumber(String fileName) {
        if (fileName == null) {
            return "BATCH_" + System.currentTimeMillis();
        }
        
        // NACH file pattern: ACH-DR-BDBL-03062024-TPZ000433633-P3FC-INW.txt
        // Try to find batch number pattern in file name
        String[] parts = fileName.split("-");
        for (String part : parts) {
            // Look for patterns like TPZ000433633, HV000004, LV000001
            if (part.matches("[A-Z]{2,3}[0-9]{6,}")) {
                return part;
            }
        }
        
        // Look for any alphanumeric pattern that could be batch
        for (String part : parts) {
            if (part.matches("[A-Z0-9]{4,}") && !part.equals("BDBL") && !part.equals("INW")) {
                return part;
            }
        }
        
        // If no pattern found, generate from timestamp and file name
        String prefix = fileName.length() > 10 ? fileName.substring(0, 4) : "BTCH";
        return prefix + "_" + System.currentTimeMillis();
    }
    
    /**
     * Clean up temporary files in upload directory
     * @param hoursOld Files older than this will be deleted
     * @param config Application configuration
     * @return Number of files deleted
     */
    public static int cleanupTempFiles(int hoursOld, ApplicationConfig config) {
        Path uploadDir = Paths.get(config.getUploadDirectory());

        if (!Files.exists(uploadDir)) {
            logger.info("Upload directory does not exist: {}", uploadDir);
            return 0;
        }

        long cutoffTime = System.currentTimeMillis() - (hoursOld * 60 * 60 * 1000L);
        AtomicInteger deletedCount = new AtomicInteger(0);

        try {
            Files.walk(uploadDir)
                 .filter(Files::isRegularFile)
                 .filter(path -> {
                     try {
                         return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                     } catch (IOException e) {
                         logger.warn("Error checking file modification time: {}", path, e);
                         return false;
                     }
                 })
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                         deletedCount.incrementAndGet();
                         logger.debug("Deleted old temp file: {}", path);
                     } catch (IOException e) {
                         logger.warn("Failed to delete temp file: {}", path, e);
                     }
                 });
        } catch (IOException e) {
            logger.error("Error during temp file cleanup in directory: {}", uploadDir, e);
        }

        if (deletedCount.get() > 0) {
            logger.info("Cleaned up {} old temp files from {}", deletedCount.get(), uploadDir);
        }
        return deletedCount.get();
    }
    
    /**
     * Get file size in human readable format
     * @param bytes File size in bytes
     * @return Human readable file size
     */
    public static String getHumanReadableFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Check if file contains valid NACH format header
     * @param lines List of lines from file
     * @return true if header is valid
     */
    public static boolean hasValidNachHeader(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return false;
        }
        
        String firstLine = lines.get(0);
        
        // Check if first line contains expected header fields
        String[] headerFields = firstLine.split(Constants.FIELD_SEPARATOR_LITERAL);
        
        // Basic validation - should have at least minimum required fields
        if (headerFields.length < Constants.MIN_FIELDS_COUNT) {
            logger.warn("Invalid header: expected at least {} fields, found {}", 
                       Constants.MIN_FIELDS_COUNT, headerFields.length);
            return false;
        }
        
        // Additional validation - check if header contains expected field names
        boolean hasExpectedFields = false;
        for (String field : headerFields) {
            String upperField = field.trim().toUpperCase();
            if (upperField.contains("TXN") || upperField.contains("MANDATE") || 
                upperField.contains("ACCOUNT") || upperField.contains("AMOUNT")) {
                hasExpectedFields = true;
                break;
            }
        }
        
        return hasExpectedFields;
    }
    
    /**
     * Validate NACH file content structure
     * @param lines List of lines from file
     * @return ValidationResult with details
     */
    public static ValidationResult validateNachFileContent(List<String> lines) {
        List<String> errors = new ArrayList<>();
        
        if (lines == null || lines.isEmpty()) {
            errors.add("File is empty");
            return new ValidationResult(false, errors);
        }
        
        // Check header
        if (!hasValidNachHeader(lines)) {
            errors.add("Invalid NACH file header format");
        }
        
        // Validate data lines
        for (int i = 1; i < lines.size(); i++) { // Skip header line
            String line = lines.get(i);
            String[] fields = line.split(Constants.FIELD_SEPARATOR_LITERAL);
            
            if (fields.length < Constants.MIN_FIELDS_COUNT) {
                errors.add("Line " + (i + 1) + ": Insufficient fields (expected at least " 
                          + Constants.MIN_FIELDS_COUNT + ", found " + fields.length + ")");
                
                // Don't validate more than 10 line errors to avoid spam
                if (errors.size() >= 10) {
                    errors.add("... and potentially more errors");
                    break;
                }
            }
        }
        
        // Check if file has data lines
        if (lines.size() <= 1) {
            errors.add("File contains header only, no transaction data found");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Get file statistics
     * @param part File part
     * @return String with file statistics
     */
    public static String getFileStatistics(Part part) {
        if (part == null) {
            return "No file";
        }
        
        String fileName = getFileName(part);
        long fileSize = part.getSize();
        String contentType = part.getContentType();
        
        return String.format("File: %s, Size: %s, Type: %s", 
                           fileName != null ? fileName : "Unknown", 
                           getHumanReadableFileSize(fileSize),
                           contentType != null ? contentType : "Unknown");
    }
}