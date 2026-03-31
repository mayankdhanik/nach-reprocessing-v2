package com.nach.servlet;

import com.nach.dao.NachTransactionDAO;
import com.nach.model.NachTransaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

public class NachTransactionServlet extends HttpServlet {

    private NachTransactionDAO transactionDAO = new NachTransactionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            String fileName = request.getParameter("fileName");

            // GET /api/nach/transactions?files=true  → return list of uploaded file names
            if ("true".equals(request.getParameter("files"))) {
                List<String> files = transactionDAO.getUploadedFiles();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < files.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(files.get(i)).append("\"");
                }
                sb.append("]");
                out.write(sb.toString());
                return;
            }

            // GET /api/nach/transactions?fileName=xxx → return txns for that file only
            List<NachTransaction> transactions = (fileName != null && !fileName.isEmpty())
                ? transactionDAO.getTransactionsByFile(fileName)
                : transactionDAO.getAllTransactions();

            out.write(toJson(transactions));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private String toJson(List<NachTransaction> transactions) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < transactions.size(); i++) {
            NachTransaction t = transactions.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
                .append("\"id\":\"").append(esc(t.getMsgId())).append("\",")
                .append("\"msgId\":\"").append(esc(t.getMsgId())).append("\",")
                .append("\"txnRefNo\":\"").append(esc(t.getTxnRefNo())).append("\",")
                .append("\"mandateId\":\"").append(esc(t.getMandateId())).append("\",")
                .append("\"fileName\":\"").append(esc(t.getFileName())).append("\",")
                .append("\"accountNo\":\"").append(esc(t.getAccountNo())).append("\",")
                .append("\"amount\":").append(t.getAmount()).append(",")
                .append("\"status\":\"").append(esc(t.getStatus())).append("\",")
                .append("\"errorCode\":\"").append(esc(t.getErrorCode())).append("\",")
                .append("\"errorDesc\":\"").append(esc(t.getErrorDesc())).append("\",")
                .append("\"processedDate\":\"").append(t.getProcessedDate() != null ? df.format(t.getProcessedDate()) : "").append("\",")
                .append("\"batchNo\":\"").append(esc(t.getBatchNo())).append("\",")
                .append("\"fileType\":\"").append(esc(t.getFileType())).append("\",")
                .append("\"reprocessable\":").append(new NachTransactionDAO().isReprocessable(t))
                .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String esc(String s) { return s != null ? s.replace("\"", "\\\"") : ""; }
}
