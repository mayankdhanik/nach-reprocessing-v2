package com.nach.dao;

import com.nach.model.NachTransaction;
import com.nach.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NachTransactionDAO {

    private static final Logger logger = LoggerFactory.getLogger(NachTransactionDAO.class);

    // Non-reprocessable error codes (business errors)
    private static final java.util.Set<String> NON_REPROCESSABLE_CODES = new java.util.HashSet<>(
        java.util.Arrays.asList("E001", "E003", "E004", "E005", "E010", "E011")
    );
    private static final java.util.Set<String> NON_REPROCESSABLE_KEYWORDS = new java.util.HashSet<>(
        java.util.Arrays.asList("insufficient", "invalid account", "duplicate", "invalid mandate", "account closed", "blocked")
    );

    // Generate MSG_ID like MSG1000000000000001
    private String generateMsgId(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 'MSG' || NACH_MSG_SEQ.NEXTVAL AS MSG_ID FROM DUAL")) {
            if (rs.next()) return rs.getString("MSG_ID");
        }
        throw new SQLException("Failed to generate MSG_ID from sequence");
    }

    public boolean isReprocessable(NachTransaction t) {
        String status = t.getStatus();
        if (!"ERROR".equals(status) && !"STUCK".equals(status) && !"FAILED".equals(status)) return false;
        if (t.getErrorCode() != null && NON_REPROCESSABLE_CODES.contains(t.getErrorCode().toUpperCase())) return false;
        if (t.getErrorDesc() != null) {
            String desc = t.getErrorDesc().toLowerCase();
            for (String kw : NON_REPROCESSABLE_KEYWORDS) {
                if (desc.contains(kw)) return false;
            }
        }
        return true;
    }

    public List<NachTransaction> getAllTransactions() {
        return queryList("SELECT * FROM NACH_TRANSACTIONS ORDER BY PROCESSED_DATE DESC", ps -> {});
    }

    public List<NachTransaction> getTransactionsByFile(String fileName) {
        return queryList("SELECT * FROM NACH_TRANSACTIONS WHERE FILE_NAME = ? ORDER BY PROCESSED_DATE DESC",
            ps -> ps.setString(1, fileName));
    }

    public List<String> getUploadedFiles() {
        List<String> files = new ArrayList<>();
        String sql = "SELECT FILE_NAME, MAX(PROCESSED_DATE) AS LAST_DATE FROM NACH_TRANSACTIONS GROUP BY FILE_NAME ORDER BY LAST_DATE DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) files.add(rs.getString("FILE_NAME"));
        } catch (SQLException e) {
            logger.error("getUploadedFiles failed", e);
        }
        return files;
    }

    public List<NachTransaction> getTransactionsByMsgIds(List<String> msgIds) {
        if (msgIds == null || msgIds.isEmpty()) return new ArrayList<>();
        String placeholders = String.join(",", msgIds.stream().map(id -> "?").toArray(String[]::new));
        return queryList("SELECT * FROM NACH_TRANSACTIONS WHERE MSG_ID IN (" + placeholders + ")",
            ps -> { for (int i = 0; i < msgIds.size(); i++) ps.setString(i + 1, msgIds.get(i)); });
    }

    public boolean transactionExists(String txnRefNo) {
        String sql = "SELECT COUNT(*) FROM NACH_TRANSACTIONS WHERE TXN_REF_NO = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txnRefNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("transactionExists failed for: {}", txnRefNo, e);
            return false;
        }
    }

    public boolean insertTransaction(NachTransaction t) {
        if (transactionExists(t.getTxnRefNo())) {
            logger.debug("Skipping duplicate txnRefNo: {}", t.getTxnRefNo());
            return false;
        }
        String sql = "INSERT INTO NACH_TRANSACTIONS " +
                "(MSG_ID,TXN_REF_NO,MANDATE_ID,FILE_NAME,ACCOUNT_NO,AMOUNT,STATUS,ERROR_CODE,ERROR_DESC,BATCH_NO,FILE_TYPE,PROCESSED_DATE) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String msgId = generateMsgId(conn);
            ps.setString(1, msgId);
            ps.setString(2, t.getTxnRefNo());
            ps.setString(3, t.getMandateId());
            ps.setString(4, t.getFileName());
            ps.setString(5, t.getAccountNo());
            ps.setBigDecimal(6, t.getAmount());
            ps.setString(7, t.getStatus());
            ps.setString(8, t.getErrorCode());
            ps.setString(9, t.getErrorDesc());
            ps.setString(10, t.getBatchNo());
            ps.setString(11, t.getFileType());
            ps.setTimestamp(12, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            t.setMsgId(msgId);
            return true;
        } catch (SQLException e) {
            logger.error("insertTransaction failed for: {}", t.getTxnRefNo(), e);
            return false;
        }
    }

    public boolean updateTransactionStatus(String msgId, String status) {
        String oldStatus = null;
        String txnRefNo = null;
        String fileName = null;
        String selectSql = "SELECT STATUS, TXN_REF_NO, FILE_NAME FROM NACH_TRANSACTIONS WHERE MSG_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, msgId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    oldStatus = rs.getString("STATUS");
                    txnRefNo = rs.getString("TXN_REF_NO");
                    fileName = rs.getString("FILE_NAME");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch old status for msgId: {}", msgId, e);
        }

        String sql = "UPDATE NACH_TRANSACTIONS SET STATUS=?, ERROR_CODE=NULL, ERROR_DESC=NULL, UPDATED_DATE=? WHERE MSG_ID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, msgId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) insertReprocessAudit(msgId, txnRefNo, fileName, oldStatus, status);
            return updated;
        } catch (SQLException e) {
            logger.error("updateTransactionStatus failed for msgId: {}", msgId, e);
            return false;
        }
    }

    private void insertReprocessAudit(String txnMsgId, String txnRefNo, String fileName, String oldStatus, String newStatus) {
        String sql = "INSERT INTO NACH_REPROCESS_AUDIT (MSG_ID, TXN_MSG_ID, TXN_REF_NO, FILE_NAME, OLD_STATUS, NEW_STATUS, REPROCESS_DATE, REMARKS) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateMsgId(conn));
            ps.setString(2, txnMsgId);
            ps.setString(3, txnRefNo);
            ps.setString(4, fileName);
            ps.setString(5, oldStatus);
            ps.setString(6, newStatus);
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.setString(8, "Reprocessed via NACH system");
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("insertReprocessAudit failed for txnMsgId: {}", txnMsgId, e);
        }
    }

    public void insertFileUploadAudit(String fileName, String fileType, int totalCount, int successCount, int duplicateCount) {
        String sql = "INSERT INTO NACH_FILE_UPLOADS (MSG_ID, FILE_NAME, FILE_TYPE, UPLOAD_DATE, TOTAL_COUNT, SUCCESS_COUNT, DUPLICATE_COUNT) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateMsgId(conn));
            ps.setString(2, fileName);
            ps.setString(3, fileType);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setInt(5, totalCount);
            ps.setInt(6, successCount);
            ps.setInt(7, duplicateCount);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("insertFileUploadAudit failed for file: {}", fileName, e);
        }
    }

    private List<NachTransaction> queryList(String sql, SqlSetter setter) {
        List<NachTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Query failed: {}", sql, e);
        }
        return list;
    }

    @FunctionalInterface
    interface SqlSetter { void set(PreparedStatement ps) throws SQLException; }

    private NachTransaction map(ResultSet rs) throws SQLException {
        NachTransaction t = new NachTransaction();
        t.setMsgId(rs.getString("MSG_ID"));
        t.setTxnRefNo(rs.getString("TXN_REF_NO"));
        t.setMandateId(rs.getString("MANDATE_ID"));
        t.setFileName(rs.getString("FILE_NAME"));
        t.setAccountNo(rs.getString("ACCOUNT_NO"));
        t.setAmount(rs.getBigDecimal("AMOUNT"));
        t.setStatus(rs.getString("STATUS"));
        t.setErrorCode(rs.getString("ERROR_CODE"));
        t.setErrorDesc(rs.getString("ERROR_DESC"));
        t.setProcessedDate(rs.getTimestamp("PROCESSED_DATE"));
        t.setBatchNo(rs.getString("BATCH_NO"));
        t.setFileType(rs.getString("FILE_TYPE"));
        return t;
    }
}
