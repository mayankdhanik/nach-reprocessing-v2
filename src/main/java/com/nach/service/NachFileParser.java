package com.nach.service;

import com.nach.model.NachTransaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NachFileParser {
    
    public List<NachTransaction> parseNachFile(InputStream fileInputStream, String fileName) throws IOException {
        List<NachTransaction> transactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            String line;
            int lineNumber = 0;
            String fileType = fileName.contains("DR") ? "DR" : "CR";
            String batchNo = extractBatchNo(fileName);
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip header line
                if (lineNumber == 1) continue;
                
                try {
                    NachTransaction transaction = parseLine(line, fileName, fileType, batchNo);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                } catch (Exception e) {
                    // Create error transaction for invalid lines
                    NachTransaction errorTxn = new NachTransaction();
                    errorTxn.setTxnRefNo("ERR_" + UUID.randomUUID().toString().substring(0, 8));
                    errorTxn.setFileName(fileName);
                    errorTxn.setStatus("PARSE_ERROR");
                    errorTxn.setErrorDesc("Line parsing failed: " + e.getMessage());
                    errorTxn.setFileType(fileType);
                    errorTxn.setBatchNo(batchNo);
                    transactions.add(errorTxn);
                }
            }
        }
        
        return transactions;
    }
    
    private NachTransaction parseLine(String line, String fileName, String fileType, String batchNo) {
        // Sample NACH file format parsing (adjust based on actual format)
        String[] fields = line.split("\\|"); // Assuming pipe-separated values
        
        if (fields.length < 8) {
            throw new IllegalArgumentException("Invalid line format");
        }
        
        NachTransaction transaction = new NachTransaction();
        transaction.setTxnRefNo(fields[0].trim());
        transaction.setMandateId(fields[1].trim());
        transaction.setAccountNo(fields[2].trim());
        transaction.setAmount(new BigDecimal(fields[3].trim()));
        transaction.setStatus(fields[4].trim());
        transaction.setErrorCode(fields.length > 5 ? fields[5].trim() : null);
        transaction.setErrorDesc(fields.length > 6 ? fields[6].trim() : null);
        transaction.setFileName(fileName);
        transaction.setFileType(fileType);
        transaction.setBatchNo(batchNo);

        return transaction;
    }
    
    private boolean validateTransaction(NachTransaction transaction) {
        // Basic validation logic
        return transaction.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
               transaction.getAccountNo() != null && 
               transaction.getAccountNo().length() >= 10 &&
               transaction.getMandateId() != null;
    }
    
    private String extractBatchNo(String fileName) {
        // Extract batch number from filename
        if (fileName.contains("-")) {
            String[] parts = fileName.split("-");
            if (parts.length > 3) {
                return parts[3]; // Assuming batch is 4th part
            }
        }
        return "DEFAULT";
    }
}