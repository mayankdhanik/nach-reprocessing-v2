package com.nach.servlet;


import com.nach.dao.NachTransactionDAO;
import com.nach.model.NachTransaction;
import com.nach.service.NachFileParser;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class NachUploadServlet extends HttpServlet {
    
    private NachTransactionDAO transactionDAO = new NachTransactionDAO();
    private NachFileParser fileParser = new NachFileParser();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Enable CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        PrintWriter out = response.getWriter();
        
        try {
            Part filePart = request.getPart("file");
            if (filePart == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"No file uploaded\"}");
                return;
            }
            
            String fileName = getFileName(filePart);
            if (!fileName.endsWith(".txt")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Only .txt files are allowed\"}");
                return;
            }
            
            // Parse the uploaded file
            List<NachTransaction> transactions = fileParser.parseNachFile(
                filePart.getInputStream(), fileName);
            
            // Save transactions to database
            int successCount = 0;
            for (NachTransaction transaction : transactions) {
                if (transactionDAO.insertTransaction(transaction)) {
                    successCount++;
                }
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.write(String.format(
                "{\"message\": \"File processed successfully\", \"transactionCount\": %d, \"successCount\": %d}",
                transactions.size(), successCount));
                
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"File processing failed: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
    
    // Servlet 3.0 compatible way to get filename from Part
    private String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "unknown.txt";
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}