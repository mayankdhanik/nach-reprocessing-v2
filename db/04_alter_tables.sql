-- ============================================================
-- Run as nach_user in SQL Developer
-- ============================================================

-- 1. Add FILE_NAME to NACH_REPROCESS_AUDIT
ALTER TABLE NACH_REPROCESS_AUDIT ADD FILE_NAME VARCHAR2(255);

CREATE INDEX idx_audit_file_name ON NACH_REPROCESS_AUDIT (FILE_NAME);

-- 2. Make NACH_TRANSACTIONS ID start from 16 digits
ALTER TABLE NACH_TRANSACTIONS MODIFY ID GENERATED ALWAYS AS IDENTITY (START WITH 1000000000000001 INCREMENT BY 1);

COMMIT;

-- ============================================================
-- VERIFY: Check all 3 tables have FILE_NAME as common column
-- ============================================================
SELECT 'NACH_TRANSACTIONS'   AS TABLE_NAME, 'FILE_NAME' AS COMMON_COLUMN FROM DUAL
UNION ALL
SELECT 'NACH_FILE_UPLOADS'   AS TABLE_NAME, 'FILE_NAME' AS COMMON_COLUMN FROM DUAL
UNION ALL
SELECT 'NACH_REPROCESS_AUDIT' AS TABLE_NAME, 'FILE_NAME' AS COMMON_COLUMN FROM DUAL;

-- ============================================================
-- USEFUL JOIN QUERY: See full picture for any file
-- ============================================================
-- SELECT
--     u.FILE_NAME,
--     u.UPLOAD_DATE,
--     u.TOTAL_COUNT,
--     u.SUCCESS_COUNT,
--     u.DUPLICATE_COUNT,
--     t.TXN_REF_NO,
--     t.AMOUNT,
--     t.STATUS,
--     r.OLD_STATUS,
--     r.NEW_STATUS,
--     r.REPROCESS_DATE
-- FROM NACH_FILE_UPLOADS u
-- JOIN NACH_TRANSACTIONS t ON t.FILE_NAME = u.FILE_NAME
-- LEFT JOIN NACH_REPROCESS_AUDIT r ON r.FILE_NAME = u.FILE_NAME AND r.TRANSACTION_ID = t.ID
-- WHERE u.FILE_NAME = 'ACH-DR-APRIL-002.txt'
-- ORDER BY t.ID;
