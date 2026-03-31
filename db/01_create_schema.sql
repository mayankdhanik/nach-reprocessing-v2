-- ============================================================
-- STEP 1: Run this as SYSDBA or DBA user in SQL Developer
-- Creates the NACH schema user with all required privileges
-- ============================================================

-- Create new schema user
CREATE USER nach_user IDENTIFIED BY Nach@1234
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

-- Grant connect and resource
GRANT CONNECT, RESOURCE TO nach_user;

-- Grant specific privileges needed
GRANT CREATE SESSION     TO nach_user;
GRANT CREATE TABLE       TO nach_user;
GRANT CREATE SEQUENCE    TO nach_user;
GRANT CREATE VIEW        TO nach_user;
GRANT CREATE PROCEDURE   TO nach_user;
GRANT CREATE TRIGGER     TO nach_user;
GRANT CREATE INDEX       TO nach_user;

COMMIT;

-- ============================================================
-- After running this, connect as nach_user and run:
-- 02_create_tables.sql
-- ============================================================
