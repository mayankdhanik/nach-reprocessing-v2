-- ============================================================
-- STEP 2: Run this as nach_user in SQL Developer
-- Creates all tables, sequences, indexes, and constraints
-- ============================================================


-- ============================================================
-- TABLE 1: NACH_TRANSACTIONS (main transaction table)
-- ============================================================
CREATE TABLE NACH_TRANSACTIONS (
    ID              NUMBER(19)      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TXN_REF_NO      VARCHAR2(100)   NOT NULL,
    MANDATE_ID      VARCHAR2(100),
    FILE_NAME       VARCHAR2(255),
    ACCOUNT_NO      VARCHAR2(50),
    AMOUNT          NUMBER(15,2),
    STATUS          VARCHAR2(20)    CONSTRAINT chk_txn_status
                                    CHECK (STATUS IN ('SUCCESS','ERROR','STUCK','FAILED','REPROCESSED')),
    ERROR_CODE      VARCHAR2(50),
    ERROR_DESC      VARCHAR2(500),
    BATCH_NO        VARCHAR2(50),
    FILE_TYPE       VARCHAR2(10)    CONSTRAINT chk_file_type
                                    CHECK (FILE_TYPE IN ('DR','CR')),
    PROCESSED_DATE  TIMESTAMP       DEFAULT SYSTIMESTAMP,
    UPDATED_DATE    TIMESTAMP
);

-- Unique constraint on TXN_REF_NO (no duplicate transactions)
ALTER TABLE NACH_TRANSACTIONS
    ADD CONSTRAINT uq_txn_ref_no UNIQUE (TXN_REF_NO);

-- Indexes for fast lookup
CREATE INDEX idx_txn_file_name    ON NACH_TRANSACTIONS (FILE_NAME);
CREATE INDEX idx_txn_status       ON NACH_TRANSACTIONS (STATUS);
CREATE INDEX idx_txn_mandate_id   ON NACH_TRANSACTIONS (MANDATE_ID);
CREATE INDEX idx_txn_account_no   ON NACH_TRANSACTIONS (ACCOUNT_NO);
CREATE INDEX idx_txn_processed_dt ON NACH_TRANSACTIONS (PROCESSED_DATE DESC);

COMMENT ON TABLE  NACH_TRANSACTIONS              IS 'Stores all NACH debit/credit transactions';
COMMENT ON COLUMN NACH_TRANSACTIONS.ID           IS 'Auto-generated primary key';
COMMENT ON COLUMN NACH_TRANSACTIONS.TXN_REF_NO   IS 'Unique transaction reference number from file';
COMMENT ON COLUMN NACH_TRANSACTIONS.MANDATE_ID   IS 'NACH mandate ID';
COMMENT ON COLUMN NACH_TRANSACTIONS.FILE_NAME    IS 'Source file name this transaction came from';
COMMENT ON COLUMN NACH_TRANSACTIONS.ACCOUNT_NO   IS 'Bank account number';
COMMENT ON COLUMN NACH_TRANSACTIONS.AMOUNT       IS 'Transaction amount in INR';
COMMENT ON COLUMN NACH_TRANSACTIONS.STATUS       IS 'SUCCESS / ERROR / STUCK / FAILED / REPROCESSED';
COMMENT ON COLUMN NACH_TRANSACTIONS.ERROR_CODE   IS 'Error code from bank (e.g. E001, E002)';
COMMENT ON COLUMN NACH_TRANSACTIONS.ERROR_DESC   IS 'Detailed error description';
COMMENT ON COLUMN NACH_TRANSACTIONS.BATCH_NO     IS 'Batch number from file';
COMMENT ON COLUMN NACH_TRANSACTIONS.FILE_TYPE    IS 'DR = Debit, CR = Credit';
COMMENT ON COLUMN NACH_TRANSACTIONS.PROCESSED_DATE IS 'When transaction was first uploaded';
COMMENT ON COLUMN NACH_TRANSACTIONS.UPDATED_DATE IS 'When status was last updated';


-- ============================================================
-- TABLE 2: NACH_FILE_UPLOADS (track uploaded files)
-- ============================================================
CREATE TABLE NACH_FILE_UPLOADS (
    ID              NUMBER(19)      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    FILE_NAME       VARCHAR2(255)   NOT NULL,
    FILE_TYPE       VARCHAR2(10),
    UPLOAD_DATE     TIMESTAMP       DEFAULT SYSTIMESTAMP,
    TOTAL_COUNT     NUMBER(10)      DEFAULT 0,
    SUCCESS_COUNT   NUMBER(10)      DEFAULT 0,
    DUPLICATE_COUNT NUMBER(10)      DEFAULT 0,
    UPLOADED_BY     VARCHAR2(100)   DEFAULT 'SYSTEM'
);

CREATE INDEX idx_upload_file_name ON NACH_FILE_UPLOADS (FILE_NAME);
CREATE INDEX idx_upload_date      ON NACH_FILE_UPLOADS (UPLOAD_DATE DESC);

COMMENT ON TABLE  NACH_FILE_UPLOADS              IS 'Audit log of all uploaded NACH files';
COMMENT ON COLUMN NACH_FILE_UPLOADS.FILE_NAME    IS 'Uploaded file name';
COMMENT ON COLUMN NACH_FILE_UPLOADS.FILE_TYPE    IS 'DR or CR';
COMMENT ON COLUMN NACH_FILE_UPLOADS.TOTAL_COUNT  IS 'Total records in file';
COMMENT ON COLUMN NACH_FILE_UPLOADS.SUCCESS_COUNT IS 'Records successfully inserted';
COMMENT ON COLUMN NACH_FILE_UPLOADS.DUPLICATE_COUNT IS 'Records skipped as duplicates';


-- ============================================================
-- TABLE 3: NACH_REPROCESS_AUDIT (track reprocessing activity)
-- ============================================================
CREATE TABLE NACH_REPROCESS_AUDIT (
    ID              NUMBER(19)      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TRANSACTION_ID  NUMBER(19)      NOT NULL,
    TXN_REF_NO      VARCHAR2(100),
    OLD_STATUS      VARCHAR2(20),
    NEW_STATUS      VARCHAR2(20),
    REPROCESS_DATE  TIMESTAMP       DEFAULT SYSTIMESTAMP,
    REMARKS         VARCHAR2(500),
    REPROCESSED_BY  VARCHAR2(100)   DEFAULT 'SYSTEM',
    CONSTRAINT fk_reprocess_txn
        FOREIGN KEY (TRANSACTION_ID)
        REFERENCES NACH_TRANSACTIONS (ID)
);

CREATE INDEX idx_audit_txn_id     ON NACH_REPROCESS_AUDIT (TRANSACTION_ID);
CREATE INDEX idx_audit_reprocess_dt ON NACH_REPROCESS_AUDIT (REPROCESS_DATE DESC);

COMMENT ON TABLE  NACH_REPROCESS_AUDIT           IS 'Audit trail of all reprocessing actions';
COMMENT ON COLUMN NACH_REPROCESS_AUDIT.TRANSACTION_ID IS 'FK to NACH_TRANSACTIONS';
COMMENT ON COLUMN NACH_REPROCESS_AUDIT.OLD_STATUS IS 'Status before reprocessing';
COMMENT ON COLUMN NACH_REPROCESS_AUDIT.NEW_STATUS IS 'Status after reprocessing';


-- ============================================================
-- TABLE 4: NACH_ERROR_CONFIG (error code master)
-- ============================================================
CREATE TABLE NACH_ERROR_CONFIG (
    ERROR_CODE      VARCHAR2(20)    PRIMARY KEY,
    ERROR_DESC      VARCHAR2(500),
    IS_REPROCESSABLE VARCHAR2(1)   DEFAULT 'Y'
                                    CONSTRAINT chk_reprocessable
                                    CHECK (IS_REPROCESSABLE IN ('Y','N')),
    ERROR_CATEGORY  VARCHAR2(50),
    CREATED_DATE    TIMESTAMP       DEFAULT SYSTIMESTAMP
);

COMMENT ON TABLE  NACH_ERROR_CONFIG              IS 'Master config for NACH error codes';
COMMENT ON COLUMN NACH_ERROR_CONFIG.IS_REPROCESSABLE IS 'Y = can retry, N = business error do not retry';

-- Seed error config data
INSERT INTO NACH_ERROR_CONFIG VALUES ('E001', 'Insufficient funds',              'N', 'BUSINESS',  SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E002', 'Network timeout',                 'Y', 'TECHNICAL', SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E003', 'Invalid account number',          'N', 'BUSINESS',  SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E004', 'Invalid mandate',                 'N', 'BUSINESS',  SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E005', 'Duplicate transaction',           'N', 'BUSINESS',  SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E006', 'Bank server unavailable',         'Y', 'TECHNICAL', SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E007', 'Connection reset',                'Y', 'TECHNICAL', SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E008', 'Processing timeout',              'Y', 'TECHNICAL', SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E009', 'System maintenance',              'Y', 'TECHNICAL', SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E010', 'Account closed',                  'N', 'BUSINESS',  SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E011', 'Account blocked',                 'N', 'BUSINESS',  SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('E012', 'Beneficiary bank down',           'Y', 'TECHNICAL', SYSTIMESTAMP);
INSERT INTO NACH_ERROR_CONFIG VALUES ('STUCK','Transaction stuck in processing', 'Y', 'TECHNICAL', SYSTIMESTAMP);

COMMIT;


-- ============================================================
-- VERIFY: Check all tables created
-- ============================================================
SELECT TABLE_NAME, NUM_ROWS
FROM USER_TABLES
WHERE TABLE_NAME IN ('NACH_TRANSACTIONS','NACH_FILE_UPLOADS','NACH_REPROCESS_AUDIT','NACH_ERROR_CONFIG')
ORDER BY TABLE_NAME;
