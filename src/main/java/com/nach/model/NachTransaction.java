package com.nach.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Model class representing a NACH transaction
 */
public class NachTransaction {
    private Long id;
    private String txnRefNo;
    private String mandateId;
    private String fileName;
    private String accountNo;
    private BigDecimal amount;
    private String status;
    private String errorCode;
    private String errorDesc;
    private Date processedDate;
    private String batchNo;
    private String fileType;
    private Date createdDate;
    private Date updatedDate;
    
    // Default constructor
    public NachTransaction() {
        this.createdDate = new Date();
        this.updatedDate = new Date();
    }
    
    // Constructor with essential fields
    public NachTransaction(String txnRefNo, String mandateId, String fileName, 
                          String accountNo, BigDecimal amount, String status, 
                          String fileType, String batchNo) {
        this();
        this.txnRefNo = txnRefNo;
        this.mandateId = mandateId;
        this.fileName = fileName;
        this.accountNo = accountNo;
        this.amount = amount;
        this.status = status;
        this.fileType = fileType;
        this.batchNo = batchNo;
        this.processedDate = new Date();
    }
    
    // Constructor with error details
    public NachTransaction(String txnRefNo, String mandateId, String fileName, 
                          String accountNo, BigDecimal amount, String status, 
                          String errorCode, String errorDesc, String fileType, String batchNo) {
        this(txnRefNo, mandateId, fileName, accountNo, amount, status, fileType, batchNo);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTxnRefNo() {
        return txnRefNo;
    }
    
    public void setTxnRefNo(String txnRefNo) {
        this.txnRefNo = txnRefNo;
    }
    
    public String getMandateId() {
        return mandateId;
    }
    
    public void setMandateId(String mandateId) {
        this.mandateId = mandateId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getAccountNo() {
        return accountNo;
    }
    
    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.updatedDate = new Date();
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorDesc() {
        return errorDesc;
    }
    
    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }
    
    public Date getProcessedDate() {
        return processedDate;
    }
    
    public void setProcessedDate(Date processedDate) {
        this.processedDate = processedDate;
    }
    
    public String getBatchNo() {
        return batchNo;
    }
    
    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public Date getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    // Helper methods
    public boolean isReprocessable() {
        return "ERROR".equals(status) || "STUCK".equals(status) || "FAILED".equals(status);
    }
    
    public boolean hasError() {
        return errorCode != null || errorDesc != null;
    }
    
    // toString method for debugging
    @Override
    public String toString() {
        return "NachTransaction{" +
                "id=" + id +
                ", txnRefNo='" + txnRefNo + '\'' +
                ", mandateId='" + mandateId + '\'' +
                ", accountNo='" + accountNo + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", fileType='" + fileType + '\'' +
                ", processedDate=" + processedDate +
                '}';
    }
}