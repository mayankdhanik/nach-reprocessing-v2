package com.nach.servlet;

import com.nach.dao.NachTransactionDAO;
import com.nach.model.NachTransaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NachReprocessServlet extends HttpServlet {

    private NachTransactionDAO transactionDAO = new NachTransactionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            List<Long> ids = parseIds(sb.toString());
            if (ids.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"No transaction IDs provided\"}");
                return;
            }

            List<NachTransaction> transactions = transactionDAO.getTransactionsByIds(ids);

            int success = 0, skipped = 0;
            for (NachTransaction t : transactions) {
                if (!transactionDAO.isReprocessable(t)) {
                    skipped++;
                    continue;
                }
                transactionDAO.updateTransactionStatus(t.getId(), "REPROCESSED");
                success++;
            }

            out.write(String.format(
                "{\"message\": \"Reprocessing completed\", \"successCount\": %d, \"skippedCount\": %d, \"totalCount\": %d}",
                success, skipped, transactions.size()));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Reprocessing failed: " + e.getMessage() + "\"}");
        }
    }

    private List<Long> parseIds(String json) {
        List<Long> ids = new ArrayList<>();
        Matcher m = Pattern.compile("\"transactionIds\"\\s*:\\s*\\[(.*?)\\]").matcher(json);
        if (m.find()) {
            for (String id : m.group(1).split(",")) {
                try { ids.add(Long.parseLong(id.trim())); } catch (NumberFormatException ignored) {}
            }
        }
        return ids;
    }
}
