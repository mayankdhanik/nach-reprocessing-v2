import React, { useState } from "react";
import { debounce } from "../utils/helpers";

const SearchFilter = ({ filters, onFilterChange, transactionCount, totalCount }) => {
  const [searchTerm, setSearchTerm] = useState(filters.search || "");

  const debouncedSearch = debounce((value) => onFilterChange({ search: value }), 300);

  const handleSearchChange = (e) => { setSearchTerm(e.target.value); debouncedSearch(e.target.value); };

  const handleClear = () => {
    setSearchTerm("");
    onFilterChange({ status: "ALL", fileType: "ALL", search: "", dateFrom: "", dateTo: "" });
  };

  const hasActive = filters.status !== "ALL" || filters.fileType !== "ALL" || filters.search || filters.dateFrom || filters.dateTo;

  const statusOptions = ["ALL", "SUCCESS", "ERROR", "STUCK", "FAILED", "REPROCESSED"];
  const fileTypeOptions = [{ value: "ALL", label: "All Types" }, { value: "DR", label: "Debit (DR)" }, { value: "CR", label: "Credit (CR)" }];

  return (
    <div className="card" style={{ padding: "1.125rem 1.25rem" }}>
      <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", flexWrap: "wrap" }}>
        {/* Search */}
        <div style={{ position: "relative", flex: "1 1 220px", minWidth: 180 }}>
          <svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="#94a3b8" strokeWidth={2.5}
            style={{ position: "absolute", left: 9, top: "50%", transform: "translateY(-50%)", pointerEvents: "none" }}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            type="text"
            placeholder="Search transactions..."
            value={searchTerm}
            onChange={handleSearchChange}
            className="form-input"
            style={{ paddingLeft: "2rem", paddingRight: searchTerm ? "2rem" : "0.75rem" }}
          />
          {searchTerm && (
            <button onClick={() => { setSearchTerm(""); onFilterChange({ search: "" }); }}
              style={{ position: "absolute", right: 8, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", cursor: "pointer", color: "#94a3b8", fontSize: "0.875rem", padding: 0 }}>
              ✕
            </button>
          )}
        </div>

        {/* Status */}
        <select value={filters.status} onChange={(e) => onFilterChange({ status: e.target.value })}
          className="form-input" style={{ flex: "0 1 140px", minWidth: 120 }}>
          {statusOptions.map((s) => <option key={s} value={s}>{s === "ALL" ? "All Status" : s}</option>)}
        </select>

        {/* File type */}
        <select value={filters.fileType} onChange={(e) => onFilterChange({ fileType: e.target.value })}
          className="form-input" style={{ flex: "0 1 130px", minWidth: 110 }}>
          {fileTypeOptions.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>

        {/* Date From */}
        <input type="date" value={filters.dateFrom} onChange={(e) => onFilterChange({ dateFrom: e.target.value })}
          className="form-input" style={{ flex: "0 1 140px", minWidth: 120, fontSize: "0.8rem" }} />

        {/* Date To */}
        <input type="date" value={filters.dateTo} onChange={(e) => onFilterChange({ dateTo: e.target.value })}
          className="form-input" style={{ flex: "0 1 140px", minWidth: 120, fontSize: "0.8rem" }} />

        {/* Count + Clear */}
        <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginLeft: "auto", flexShrink: 0 }}>
          <span style={{ fontSize: "0.775rem", color: "#64748b", whiteSpace: "nowrap" }}>
            <strong style={{ color: "#1e293b" }}>{transactionCount}</strong> / {totalCount}
          </span>
          {hasActive && (
            <button onClick={handleClear} className="btn-secondary" style={{ padding: "0.375rem 0.75rem", fontSize: "0.775rem" }}>
              Clear
            </button>
          )}
        </div>
      </div>

      {/* Quick filter pills */}
      <div style={{ display: "flex", gap: "0.5rem", marginTop: "0.75rem", flexWrap: "wrap" }}>
        {[
          { label: "Errors", key: "status", value: "ERROR", activeColor: "#fee2e2", activeText: "#991b1b", activeBorder: "#fca5a5" },
          { label: "Stuck", key: "status", value: "STUCK", activeColor: "#fef3c7", activeText: "#92400e", activeBorder: "#fcd34d" },
          { label: "Failed", key: "status", value: "FAILED", activeColor: "#fee2e2", activeText: "#991b1b", activeBorder: "#fca5a5" },
          { label: "Success", key: "status", value: "SUCCESS", activeColor: "#dcfce7", activeText: "#166534", activeBorder: "#86efac" },
          { label: "Debit", key: "fileType", value: "DR", activeColor: "#eff6ff", activeText: "#1d4ed8", activeBorder: "#93c5fd" },
          { label: "Credit", key: "fileType", value: "CR", activeColor: "#f5f3ff", activeText: "#5b21b6", activeBorder: "#c4b5fd" },
        ].map((pill) => {
          const isActive = filters[pill.key] === pill.value;
          return (
            <button
              key={`${pill.key}-${pill.value}`}
              onClick={() => onFilterChange({ [pill.key]: isActive ? "ALL" : pill.value })}
              style={{
                padding: "0.2rem 0.7rem", borderRadius: 9999, fontSize: "0.75rem", fontWeight: 500, cursor: "pointer", transition: "all 0.15s",
                background: isActive ? pill.activeColor : "white",
                color: isActive ? pill.activeText : "#64748b",
                border: `1px solid ${isActive ? pill.activeBorder : "#e2e8f0"}`,
              }}
            >
              {pill.label}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default SearchFilter;
