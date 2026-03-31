package com.nach.listener;

import com.nach.util.DatabaseConnection;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;

public class ApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== NACH Reprocessing System Starting ===");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("=== DB Connection OK: " + DatabaseConnection.getDatabaseType() + " ===");
            System.out.println("=== NACH Reprocessing System Started ===");
        } catch (Exception e) {
            System.err.println("=== DB Connection FAILED: " + e.getMessage() + " ===");
            System.err.println("Make sure Oracle is running and tables are created via db/02_create_tables.sql");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== NACH Reprocessing System Stopped ===");
    }
}
