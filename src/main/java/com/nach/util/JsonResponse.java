package com.nach.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating standardized JSON responses
 */
public class JsonResponse {
    
    private static final Gson gson = new GsonBuilder()
            .setDateFormat(Constants.DATE_FORMAT_ISO)
            .create();
    
    private boolean success;
    private String message;
    private Object data;
    private Map<String, Object> metadata;
    private long timestamp;
    
    private JsonResponse() {
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }
    
    /**
     * Create a success response
     * @param message Success message
     * @return JsonResponse object
     */
    public static JsonResponse success(String message) {
        JsonResponse response = new JsonResponse();
        response.success = true;
        response.message = message;
        return response;
    }
    
    /**
     * Create a success response with data
     * @param message Success message
     * @param data Response data
     * @return JsonResponse object
     */
    public static JsonResponse success(String message, Object data) {
        JsonResponse response = success(message);
        response.data = data;
        return response;
    }
    
    /**
     * Create an error response
     * @param message Error message
     * @return JsonResponse object
     */
    public static JsonResponse error(String message) {
        JsonResponse response = new JsonResponse();
        response.success = false;
        response.message = message;
        return response;
    }
    
    /**
     * Create an error response with additional details
     * @param message Error message
     * @param errorDetails Error details
     * @return JsonResponse object
     */
    public static JsonResponse error(String message, Object errorDetails) {
        JsonResponse response = error(message);
        response.data = errorDetails;
        return response;
    }
    
    /**
     * Add metadata to the response
     * @param key Metadata key
     * @param value Metadata value
     * @return JsonResponse object for chaining
     */
    public JsonResponse addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    /**
     * Add multiple metadata entries
     * @param metadata Map of metadata
     * @return JsonResponse object for chaining
     */
    public JsonResponse addMetadata(Map<String, Object> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }
    
    /**
     * Convert response to JSON string
     * @return JSON string representation
     */
    public String toJson() {
        return gson.toJson(this);
    }
    
    /**
     * Create a file upload success response
     * @param fileName Name of uploaded file
     * @param transactionCount Total transactions processed
     * @param successCount Successfully processed transactions
     * @param errorCount Failed transactions
     * @return JsonResponse object
     */
    public static JsonResponse fileUploadSuccess(String fileName, int transactionCount, 
                                               int successCount, int errorCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileName", fileName);
        data.put("transactionCount", transactionCount);
        data.put("successCount", successCount);
        data.put("errorCount", errorCount);
        data.put("successRate", transactionCount > 0 ? (double) successCount / transactionCount * 100 : 0);
        
        return success(Constants.MSG_FILE_UPLOADED_SUCCESS, data);
    }
    
    /**
     * Create a reprocess success response
     * @param totalCount Total transactions to reprocess
     * @param successCount Successfully reprocessed transactions
     * @param failedCount Failed reprocessing transactions
     * @return JsonResponse object
     */
    public static JsonResponse reprocessSuccess(int totalCount, int successCount, int failedCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", totalCount);
        data.put("successCount", successCount);
        data.put("failedCount", failedCount);
        data.put("successRate", totalCount > 0 ? (double) successCount / totalCount * 100 : 0);
        
        return success(Constants.MSG_REPROCESS_SUCCESS, data);
    }
    
    /**
     * Create a transactions list response
     * @param transactions List of transactions
     * @param totalCount Total count (for pagination)
     * @return JsonResponse object
     */
    public static JsonResponse transactionsResponse(Object transactions, int totalCount) {
        return success("Transactions retrieved successfully", transactions)
                .addMetadata("totalCount", totalCount);
    }
    
    // Getters for JSON serialization
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return toJson();
    }
}