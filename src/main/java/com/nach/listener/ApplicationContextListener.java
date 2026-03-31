package com.nach.listener;

import com.nach.util.DatabaseConnection;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.Statement;

public class ApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== NACH Reprocessing System Starting ===");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            try {
                stmt.execute(
                    "CREATE TABLE NACH_TRANSACTIONS (" +
                    "  ID             BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  TXN_REF_NO     VARCHAR(100) NOT NULL UNIQUE," +
                    "  MANDATE_ID     VARCHAR(100)," +
                    "  FILE_NAME      VARCHAR(255)," +
                    "  ACCOUNT_NO     VARCHAR(50)," +
                    "  AMOUNT         DECIMAL(15,2)," +
                    "  STATUS         VARCHAR(20)," +
                    "  ERROR_CODE     VARCHAR(50)," +
                    "  ERROR_DESC     VARCHAR(500)," +
                    "  BATCH_NO       VARCHAR(50)," +
                    "  FILE_TYPE      VARCHAR(10)," +
                    "  PROCESSED_DATE TIMESTAMP," +
                    "  UPDATED_DATE   TIMESTAMP" +
                    ")"
                );
                System.out.println("Table NACH_TRANSACTIONS created.");
            } catch (Exception e1) {
                System.out.println("NACH_TRANSACTIONS already exists.");
            }
            try {
                stmt.execute(
                    "CREATE TABLE NACH_FILE_UPLOADS (" +
                    "  ID            BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  FILE_NAME     VARCHAR(255) NOT NULL," +
                    "  UPLOAD_DATE   TIMESTAMP," +
                    "  TOTAL_COUNT   INT," +
                    "  SUCCESS_COUNT INT" +
                    ")"
                );
                System.out.println("Table NACH_FILE_UPLOADS created.");
            } catch (Exception e2) {
                System.out.println("NACH_FILE_UPLOADS already exists.");
            }

            System.out.println("=== DB ready. NACH System Started ===");
        } catch (Exception e) {
            System.err.println("Startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== NACH Reprocessing System Stopped ===");
    }
}
