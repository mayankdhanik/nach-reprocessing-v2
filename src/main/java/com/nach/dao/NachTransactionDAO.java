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

    public boolean isReprocessable(NachTransaction t) {
        String status = t.getStatus();
        if (!"ERROR".equals(status) && !"STUCK".equals(status) && !"FAILED".equals(status)) return false;

        // Block known business error codes
        if (t.getErrorCode() != null && NON_REPROCESSABLE_CODES.contains(t.getErrorCode().toUpperCase())) return false;

        // Block known business error descriptions
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

    public List<NachTransaction> getTransactionsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        String placeholders = String.join(",", ids.stream().map(id -> "?").toArray(String[]::new));
        return queryList("SELECT * FROM NACH_TRANSACTIONS WHERE ID IN (" + placeholders + ")",
            ps -> { for (int i = 0; i < ids.size(); i++) ps.setLong(i + 1, ids.get(i)); });
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
                "(TXN_REF_NO,MANDATE_ID,FILE_NAME,ACCOUNT_NO,AMOUNT,STATUS,ERROR_CODE,ERROR_DESC,BATCH_NO,FILE_TYPE,PROCESSED_DATE) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTxnRefNo());
            ps.setString(2, t.getMandateId());
            ps.setString(3, t.getFileName());
            ps.setString(4, t.getAccountNo());
            ps.setBigDecimal(5, t.getAmount());
            ps.setString(6, t.getStatus());
            ps.setString(7, t.getErrorCode());
            ps.setString(8, t.getErrorDesc());
            ps.setString(9, t.getBatchNo());
            ps.setString(10, t.getFileType());
            ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("insertTransaction failed for: {}", t.getTxnRefNo(), e);
            return false;
        }
    }

    public boolean updateTransactionStatus(Long id, String status) {
        String sql = "UPDATE NACH_TRANSACTIONS SET STATUS=?, ERROR_CODE=NULL, ERROR_DESC=NULL, UPDATED_DATE=? WHERE ID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("updateTransactionStatus failed for id: {}", id, e);
            return false;
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
        t.setId(rs.getLong("ID"));
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
