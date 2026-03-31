import React, { useState } from "react";
import { formatCurrency, formatDateShort, getStatusColorClass, getFileTypeBadgeColor, copyToClipboard, downloadCSV } from "../utils/helpers";

const TransactionTable = ({ transactions, selectedTransactions, onTransactionSelect, onSelectAll, onSort, sortConfig, loading }) => {
  const [copiedField, setCopiedField] = useState(null);

  const handleCopy = async (text, fieldId) => {
    const ok = await copyToClipboard(text);
    if (ok) { setCopiedField(fieldId); setTimeout(() => setCopiedField(null), 1500); }
  };

  const handleDownloadCSV = () => {
    if (!transactions.length) return;
    downloadCSV(transactions.map((t) => ({
      "Txn Ref": t.txnRefNo, "Mandate ID": t.mandateId, "Account No": t.accountNo,
      Amount: t.amount, Status: t.status, "Error Code": t.errorCode || "",
      "Error Desc": t.errorDesc || "", "File Name": t.fileName, "File Type": t.fileType,
      Batch: t.batchNo, Date: t.processedDate,
    })), `nach-transactions-${new Date().toISOString().split("T")[0]}.csv`);
  };

  const SortIcon = ({ field }) => {
    if (sortConfig.field !== field) return <span style={{ color: "#cbd5e1", fontSize: "0.65rem" }}>↕</span>;
    return <span style={{ color: "#3b82f6", fontSize: "0.65rem" }}>{sortConfig.direction === "asc" ? "↑" : "↓"}</span>;
  };

  const CopyBtn = ({ text, id }) => (
    <button onClick={() => handleCopy(text, id)}
      style={{ background: "none", border: "none", cursor: "pointer", color: copiedField === id ? "#10b981" : "#cbd5e1", fontSize: "0.7rem", padding: "0 2px", transition: "color 0.15s" }}
      title="Copy">
      {copiedField === id ? "✓" : "⎘"}
    </button>
  );

  const fileTypeBadge = (type) => {
    const styles = { DR: { bg: "#fef2f2", color: "#991b1b" }, CR: { bg: "#f0fdf4", color: "#166534" } };
    const s = styles[type] || { bg: "#f1f5f9", color: "#475569" };
    return (
      <span style={{ background: s.bg, color: s.color, padding: "0.15rem 0.5rem", borderRadius: 9999, fontSize: "0.7rem", fontWeight: 700 }}>
        {type}
      </span>
    );
  };

  if (loading) {
    return (
      <div className="card">
        <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
          <div style={{ height: 12, background: "#f1f5f9", borderRadius: 4, width: "25%" }} />
          {[...Array(5)].map((_, i) => <div key={i} style={{ height: 44, background: "#f8fafc", borderRadius: 6 }} />)}
        </div>
      </div>
    );
  }

  if (!transactions.length) {
    return (
      <div className="card" style={{ textAlign: "center", padding: "3rem 1.5rem" }}>
        <div style={{ fontSize: "2.5rem", marginBottom: "0.75rem" }}>📭</div>
        <h3 style={{ fontSize: "0.95rem", fontWeight: 600, color: "#1e293b", marginBottom: "0.375rem" }}>No transactions found</h3>
        <p style={{ fontSize: "0.8rem", color: "#94a3b8" }}>Adjust your filters or upload a NACH file to get started.</p>
      </div>
    );
  }

  const columns = [
    { field: "status", label: "Status" },
    { field: "txnRefNo", label: "Txn Ref No" },
    { field: "mandateId", label: "Mandate ID" },
    { field: "accountNo", label: "Account No" },
    { field: "amount", label: "Amount" },
    { field: "processedDate", label: "Date" },
  ];

  const totalAmount = transactions.reduce((s, t) => s + (parseFloat(t.amount) || 0), 0);

  return (
    <div className="card" style={{ padding: 0, overflow: "hidden" }}>
      {/* Table header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1rem 1.25rem", borderBottom: "1px solid #f1f5f9" }}>
        <div>
          <h2 style={{ fontSize: "0.9rem", fontWeight: 600, color: "#0f172a" }}>Transactions</h2>
          <p style={{ fontSize: "0.75rem", color: "#64748b", marginTop: 2 }}>
            {transactions.length} records · {selectedTransactions.length} selected · Total: {formatCurrency(totalAmount)}
          </p>
        </div>
        <button onClick={handleDownloadCSV} className="btn-secondary" style={{ fontSize: "0.775rem", padding: "0.375rem 0.875rem" }}>
          Export CSV
        </button>
      </div>

      {/* Table */}
      <div className="table-container">
        <table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <tr>
              <th className="table-header" style={{ width: 40 }}>
                <input type="checkbox"
                  checked={selectedTransactions.length === transactions.length && transactions.length > 0}
                  onChange={onSelectAll}
                  style={{ cursor: "pointer", accentColor: "#2563eb" }}
                />
              </th>
              {columns.map((col) => (
                <th key={col.field} className="table-header" style={{ cursor: "pointer" }} onClick={() => onSort(col.field)}>
                  <span style={{ display: "inline-flex", alignItems: "center", gap: 4 }}>
                    {col.label} <SortIcon field={col.field} />
                  </span>
                </th>
              ))}
              <th className="table-header">Type</th>
              <th className="table-header">Error</th>
              <th className="table-header">Action</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((t) => (
              <tr key={t.id} className="table-row"
                style={{ background: selectedTransactions.includes(t.id) ? "#eff6ff" : "white" }}>
                <td className="table-cell">
                  <input type="checkbox"
                    checked={selectedTransactions.includes(t.id)}
                    onChange={() => onTransactionSelect(t.id)}
                    style={{ cursor: "pointer", accentColor: "#2563eb" }}
                  />
                </td>
                <td className="table-cell">
                  <span className={getStatusColorClass(t.status === "REPROCESSED" ? "SUCCESS" : t.status)}>
                    {t.status === "REPROCESSED" ? "SUCCESS" : t.status}
                  </span>
                </td>
                <td className="table-cell">
                  <span style={{ color: "#2563eb", fontWeight: 500, fontSize: "0.8rem" }}>{t.txnRefNo}</span>
                  <CopyBtn text={t.txnRefNo} id={`txn-${t.id}`} />
                </td>
                <td className="table-cell" style={{ color: "#475569", fontSize: "0.8rem" }}>
                  {t.mandateId}
                  <CopyBtn text={t.mandateId} id={`mandate-${t.id}`} />
                </td>
                <td className="table-cell" style={{ fontFamily: "monospace", fontSize: "0.8rem", color: "#475569" }}>
                  {t.accountNo}
                  <CopyBtn text={t.accountNo} id={`acc-${t.id}`} />
                </td>
                <td className="table-cell" style={{ fontWeight: 600, color: "#0f172a" }}>
                  {formatCurrency(t.amount)}
                </td>
                <td className="table-cell" style={{ color: "#64748b", fontSize: "0.8rem" }}>
                  {formatDateShort(t.processedDate)}
                </td>
                <td className="table-cell">{fileTypeBadge(t.fileType)}</td>
                <td className="table-cell">
                  {t.errorCode || t.errorDesc ? (
                    <div>
                      {t.errorCode && (
                        <span style={{ background: "#fee2e2", color: "#991b1b", fontSize: "0.68rem", fontFamily: "monospace", padding: "0.1rem 0.4rem", borderRadius: 4, display: "inline-block", marginBottom: 2 }}>
                          {t.errorCode}
                        </span>
                      )}
                      {t.errorDesc && (
                        <div style={{ fontSize: "0.72rem", color: "#64748b", maxWidth: 180, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }} title={t.errorDesc}>
                          {t.errorDesc}
                        </div>
                      )}
                    </div>
                  ) : (
                    <span style={{ color: "#cbd5e1", fontSize: "0.75rem" }}>—</span>
                  )}
                </td>
                <td className="table-cell">
                  {t.reprocessable ? (
                    <button
                      onClick={() => onTransactionSelect(t.id)}
                      style={{ background: selectedTransactions.includes(t.id) ? "#dcfce7" : "#f0fdf4", color: "#16a34a", border: "1px solid #bbf7d0", borderRadius: "0.375rem", padding: "0.25rem 0.6rem", fontSize: "0.72rem", fontWeight: 600, cursor: "pointer" }}
                    >
                      {selectedTransactions.includes(t.id) ? "Selected" : "Select"}
                    </button>
                  ) : ["ERROR", "STUCK", "FAILED"].includes(t.status) ? (
                    <span style={{ fontSize: "0.7rem", color: "#94a3b8", fontStyle: "italic" }}>Not eligible</span>
                  ) : null}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TransactionTable;
