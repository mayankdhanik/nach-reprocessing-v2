import React, { useState, useEffect, useCallback } from "react";
import Dashboard from "./components/Dashboard";
import FileUpload from "./components/FileUpload";
import TransactionTable from "./components/TransactionTables";
import SearchFilter from "./components/SearchFilter";
import { nachAPI } from "./services/api";
import { filterTransactions, sortTransactions, calculateStats } from "./utils/helpers";
import "./styles/index.css";

function FileSelector({ files, selectedFile, onSelect }) {
  const [search, setSearch] = React.useState("");
  const [open, setOpen] = React.useState(false);
  const ref = React.useRef(null);

  const filtered = files.filter(f => f.toLowerCase().includes(search.toLowerCase()));

  // Close dropdown on outside click
  React.useEffect(() => {
    const handler = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const handleSelect = (f) => { onSelect(f); setSearch(""); setOpen(false); };

  return (
    <div className="card" style={{ padding: "1rem 1.25rem" }}>
      <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
        <p style={{ fontSize: "0.75rem", fontWeight: 600, color: "#64748b", textTransform: "uppercase", letterSpacing: "0.05em", flexShrink: 0 }}>
          Select File
        </p>

        {/* Searchable dropdown */}
        <div ref={ref} style={{ position: "relative", flex: 1, maxWidth: 420 }}>
          <div style={{ position: "relative" }}>
            <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="#94a3b8" strokeWidth={2.5}
              style={{ position: "absolute", left: 10, top: "50%", transform: "translateY(-50%)", pointerEvents: "none" }}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder={selectedFile || "Search and select a file..."}
              value={search}
              onFocus={() => setOpen(true)}
              onChange={e => { setSearch(e.target.value); setOpen(true); }}
              style={{
                width: "100%", paddingLeft: "2rem", paddingRight: "2.5rem",
                padding: "0.5rem 2.5rem 0.5rem 2rem",
                border: "1px solid #e2e8f0", borderRadius: "0.5rem",
                fontSize: "0.85rem", color: "#1e293b", outline: "none",
                background: "white", cursor: "pointer",
                boxShadow: open ? "0 0 0 3px rgba(59,130,246,0.1)" : "none",
                borderColor: open ? "#3b82f6" : "#e2e8f0",
              }}
            />
            <span style={{ position: "absolute", right: 10, top: "50%", transform: "translateY(-50%)", color: "#94a3b8", fontSize: "0.75rem", pointerEvents: "none" }}>
              {open ? "▲" : "▼"}
            </span>
          </div>

          {/* Dropdown list */}
          {open && (
            <div style={{
              position: "absolute", top: "calc(100% + 4px)", left: 0, right: 0, zIndex: 100,
              background: "white", border: "1px solid #e2e8f0", borderRadius: "0.5rem",
              boxShadow: "0 8px 24px rgba(0,0,0,0.1)", maxHeight: 220, overflowY: "auto"
            }}>
              {filtered.length === 0 ? (
                <div style={{ padding: "0.75rem 1rem", fontSize: "0.8rem", color: "#94a3b8" }}>No files found</div>
              ) : (
                filtered.map(f => (
                  <div key={f} onClick={() => handleSelect(f)}
                    style={{
                      padding: "0.625rem 1rem", fontSize: "0.8rem", cursor: "pointer",
                      background: selectedFile === f ? "#eff6ff" : "white",
                      color: selectedFile === f ? "#2563eb" : "#374151",
                      fontWeight: selectedFile === f ? 600 : 400,
                      borderBottom: "1px solid #f8fafc",
                      display: "flex", alignItems: "center", gap: "0.5rem",
                    }}
                    onMouseEnter={e => e.currentTarget.style.background = selectedFile === f ? "#eff6ff" : "#f8fafc"}
                    onMouseLeave={e => e.currentTarget.style.background = selectedFile === f ? "#eff6ff" : "white"}
                  >
                    <span style={{ fontSize: "0.75rem" }}>📄</span>
                    {f}
                    {selectedFile === f && <span style={{ marginLeft: "auto", fontSize: "0.7rem", color: "#2563eb" }}>✓ Selected</span>}
                  </div>
                ))
              )}
            </div>
          )}
        </div>

        {/* Selected file badge */}
        {selectedFile && (
          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", background: "#eff6ff", border: "1px solid #bfdbfe", borderRadius: "0.5rem", padding: "0.375rem 0.75rem", flexShrink: 0 }}>
            <span style={{ fontSize: "0.75rem" }}>📄</span>
            <span style={{ fontSize: "0.78rem", fontWeight: 600, color: "#2563eb", maxWidth: 200, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{selectedFile}</span>
            <button onClick={() => { onSelect(null); setSearch(""); }}
              style={{ background: "none", border: "none", cursor: "pointer", color: "#93c5fd", fontSize: "0.75rem", padding: 0 }}>✕</button>
          </div>
        )}
      </div>
    </div>
  );
}

function App() {
  const [uploadedFiles, setUploadedFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [selectedTransactions, setSelectedTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [reprocessing, setReprocessing] = useState(false);
  const [filters, setFilters] = useState({ status: "ALL", fileType: "ALL", search: "", dateFrom: "", dateTo: "" });
  const [sortConfig, setSortConfig] = useState({ field: "processedDate", direction: "desc" });
  const [stats, setStats] = useState({});
  const [toast, setToast] = useState(null);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const loadFiles = useCallback(async () => {
    try {
      const res = await nachAPI.getUploadedFiles();
      setUploadedFiles(res.data);
    } catch (e) {
      setUploadedFiles([]);
    }
  }, []);

  const loadTransactions = useCallback(async (fileName) => {
    if (!fileName) { setTransactions([]); return; }
    setLoading(true);
    try {
      const res = await nachAPI.getTransactionsByFile(fileName);
      setTransactions(res.data);
    } catch (e) {
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadFiles(); }, [loadFiles]);

  useEffect(() => {
    if (selectedFile) loadTransactions(selectedFile);
    else setTransactions([]);
    setSelectedTransactions([]);
    setFilters({ status: "ALL", fileType: "ALL", search: "", dateFrom: "", dateTo: "" });
  }, [selectedFile, loadTransactions]);

  useEffect(() => {
    const filtered = filterTransactions(transactions, filters);
    const sorted = sortTransactions(filtered, sortConfig.field, sortConfig.direction);
    setFilteredTransactions(sorted);
    setStats(calculateStats(sorted));
  }, [transactions, filters, sortConfig]);

  const handleFileUpload = async (file) => {
    setUploading(true);
    try {
      const res = await nachAPI.uploadFile(file);
      showToast(`${file.name} — ${res.data.successCount} new transactions loaded`);
      await loadFiles();
      setSelectedFile(file.name);
    } catch (e) {
      showToast("Upload failed: " + (e.response?.data?.error || e.message), "error");
    } finally {
      setUploading(false);
    }
  };

  const handleReprocess = async () => {
    if (selectedTransactions.length === 0) return;
    setReprocessing(true);
    try {
      const res = await nachAPI.reprocessTransactions(selectedTransactions);
      const { successCount, skippedCount } = res.data;
      showToast(`${successCount} reprocessed successfully${skippedCount > 0 ? `, ${skippedCount} skipped (business errors)` : ""}`);
      setSelectedTransactions([]);
      await loadTransactions(selectedFile);
    } catch (e) {
      showToast("Reprocessing failed", "error");
    } finally {
      setReprocessing(false);
    }
  };

  const handleTransactionSelect = (id) => {
    setSelectedTransactions((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);
  };

  const handleSelectAll = () => {
    const reprocessableIds = filteredTransactions.filter(t => t.reprocessable).map(t => t.id);
    const allSelected = reprocessableIds.every(id => selectedTransactions.includes(id));
    setSelectedTransactions(allSelected ? [] : reprocessableIds);
  };

  const handleSortChange = (field) => {
    setSortConfig((prev) => ({ field, direction: prev.field === field && prev.direction === "asc" ? "desc" : "asc" }));
  };

  const handleRefresh = async () => {
    setSelectedTransactions([]);
    await loadFiles();
    if (selectedFile) await loadTransactions(selectedFile);
  };

  return (
    <div style={{ minHeight: "100vh", background: "#f1f5f9" }}>
      {/* Toast */}
      {toast && (
        <div style={{
          position: "fixed", top: 16, right: 16, zIndex: 9999,
          background: toast.type === "error" ? "#dc2626" : "#059669",
          color: "white", padding: "0.75rem 1.25rem", borderRadius: "0.5rem",
          fontSize: "0.85rem", fontWeight: 500, boxShadow: "0 4px 12px rgba(0,0,0,0.15)"
        }}>
          {toast.message}
        </div>
      )}

      {/* Header */}
      <header style={{ background: "linear-gradient(135deg, #1e40af 0%, #2563eb 100%)", boxShadow: "0 2px 8px rgba(37,99,235,0.3)" }}>
        <div style={{ maxWidth: 1280, margin: "0 auto", padding: "0 1.5rem" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1rem 0" }}>
            <div>
              <h1 style={{ color: "white", fontSize: "1.375rem", fontWeight: 700 }}>NACH Reprocessing System</h1>
              <p style={{ color: "rgba(255,255,255,0.7)", fontSize: "0.8rem", marginTop: "0.125rem" }}>
                Manage and reprocess NACH transaction files
              </p>
            </div>
            <button onClick={handleRefresh} disabled={loading}
              style={{ background: "rgba(255,255,255,0.15)", color: "white", border: "1px solid rgba(255,255,255,0.25)", padding: "0.4rem 1rem", borderRadius: "0.5rem", cursor: "pointer", fontSize: "0.8rem", fontWeight: 500 }}>
              Refresh
            </button>
          </div>
        </div>
      </header>

      <main style={{ maxWidth: 1280, margin: "0 auto", padding: "1.5rem", display: "flex", flexDirection: "column", gap: "1.25rem" }}>

        {/* Upload */}
        <FileUpload onFileUpload={handleFileUpload} uploading={uploading} />

        {/* File Selector with Search */}
        {uploadedFiles.length > 0 && (
          <FileSelector
            files={uploadedFiles}
            selectedFile={selectedFile}
            onSelect={setSelectedFile}
          />
        )}

        {/* Content only when a file is selected */}
        {selectedFile ? (
          <>
            <Dashboard stats={stats} loading={loading} />
            <SearchFilter filters={filters} onFilterChange={(f) => setFilters((p) => ({ ...p, ...f }))}
              transactionCount={filteredTransactions.length} totalCount={transactions.length} />

            {/* Action bar */}
            <div className="card" style={{ padding: "0.875rem 1.25rem" }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                  <button onClick={handleSelectAll} className="btn-secondary" style={{ padding: "0.375rem 0.875rem", fontSize: "0.8rem" }}
                    disabled={filteredTransactions.filter(t => t.reprocessable).length === 0}>
                    Select Reprocessable
                  </button>
                  <span style={{ fontSize: "0.8rem", color: "#64748b" }}>
                    {selectedTransactions.length > 0
                      ? <><strong style={{ color: "#1e293b" }}>{selectedTransactions.length}</strong> selected</>
                      : `${filteredTransactions.length} transactions`}
                  </span>
                </div>
                <button onClick={handleReprocess}
                  disabled={selectedTransactions.length === 0 || reprocessing}
                  className={selectedTransactions.length === 0 || reprocessing ? "btn-secondary" : "btn-success"}
                  style={selectedTransactions.length === 0 ? { opacity: 0.5 } : {}}>
                  {reprocessing ? <><span className="spinner" /> Reprocessing...</> : "Reprocess Selected"}
                </button>
              </div>
            </div>

            <TransactionTable
              transactions={filteredTransactions}
              selectedTransactions={selectedTransactions}
              onTransactionSelect={handleTransactionSelect}
              onSelectAll={handleSelectAll}
              onSort={handleSortChange}
              sortConfig={sortConfig}
              loading={loading}
            />
          </>
        ) : (
          <div className="card" style={{ textAlign: "center", padding: "3rem 1.5rem" }}>
            <div style={{ fontSize: "2.5rem", marginBottom: "0.75rem" }}>📂</div>
            <h3 style={{ fontSize: "0.95rem", fontWeight: 600, color: "#1e293b", marginBottom: "0.375rem" }}>No file selected</h3>
            <p style={{ fontSize: "0.8rem", color: "#94a3b8" }}>Upload a NACH file or select an uploaded file above to view transactions.</p>
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
