import React, { useState, useRef } from "react";
import { validateFileType, validateFileSize, showNotification } from "../utils/helpers";

const FileUpload = ({ onFileUpload, uploading }) => {
  const [dragOver, setDragOver] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const fileInputRef = useRef(null);

  const handleFileSelect = (file) => {
    if (!validateFileType(file)) { showNotification("Only .txt files are supported", "error"); return; }
    if (!validateFileSize(file)) { showNotification("File size must be under 10MB", "error"); return; }
    setSelectedFile(file);
  };

  const handleDrop = (e) => { e.preventDefault(); setDragOver(false); if (e.dataTransfer.files[0]) handleFileSelect(e.dataTransfer.files[0]); };
  const handleUpload = () => { if (selectedFile) { onFileUpload(selectedFile); setSelectedFile(null); if (fileInputRef.current) fileInputRef.current.value = ""; } };
  const handleCancel = () => { setSelectedFile(null); if (fileInputRef.current) fileInputRef.current.value = ""; };

  return (
    <div className="card" style={{ padding: "1.25rem" }}>
      <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
        <div style={{ flex: 1 }}>
          <h2 style={{ fontSize: "0.9rem", fontWeight: 600, color: "#0f172a", marginBottom: "0.25rem" }}>Upload NACH File</h2>
          <p style={{ fontSize: "0.775rem", color: "#64748b" }}>Upload a .txt NACH transaction file to reprocess failed transactions</p>
        </div>

        {/* Compact upload zone */}
        <div
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={(e) => { e.preventDefault(); setDragOver(false); }}
          onDrop={handleDrop}
          style={{
            display: "flex", alignItems: "center", gap: "0.75rem",
            border: `2px dashed ${dragOver ? "#3b82f6" : "#e2e8f0"}`,
            background: dragOver ? "#eff6ff" : "#f8fafc",
            borderRadius: "0.625rem", padding: "0.75rem 1.25rem",
            transition: "all 0.15s", minWidth: 320
          }}
        >
          {selectedFile ? (
            <>
              <div style={{ background: "#dcfce7", borderRadius: "0.5rem", padding: "0.5rem", flexShrink: 0 }}>
                <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="#16a34a" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ fontSize: "0.8rem", fontWeight: 600, color: "#0f172a", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{selectedFile.name}</p>
                <p style={{ fontSize: "0.7rem", color: "#64748b" }}>{(selectedFile.size / 1024).toFixed(1)} KB</p>
              </div>
              <button onClick={handleUpload} disabled={uploading} className="btn-primary" style={{ padding: "0.375rem 0.875rem", fontSize: "0.8rem" }}>
                {uploading ? <><span className="spinner" />Uploading...</> : "Upload"}
              </button>
              <button onClick={handleCancel} disabled={uploading} className="btn-secondary" style={{ padding: "0.375rem 0.75rem", fontSize: "0.8rem" }}>Cancel</button>
            </>
          ) : (
            <>
              <div style={{ background: "#f1f5f9", borderRadius: "0.5rem", padding: "0.5rem", flexShrink: 0 }}>
                <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="#64748b" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                </svg>
              </div>
              <div style={{ flex: 1 }}>
                <p style={{ fontSize: "0.8rem", color: "#475569" }}>Drag & drop or</p>
                <p style={{ fontSize: "0.7rem", color: "#94a3b8" }}>.txt files only · max 10MB</p>
              </div>
              <input ref={fileInputRef} type="file" accept=".txt" onChange={(e) => e.target.files[0] && handleFileSelect(e.target.files[0])} className="hidden" id="file-upload" disabled={uploading} style={{ display: "none" }} />
              <label htmlFor="file-upload" className="btn-primary" style={{ padding: "0.375rem 0.875rem", fontSize: "0.8rem", cursor: "pointer" }}>
                Browse File
              </label>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default FileUpload;
