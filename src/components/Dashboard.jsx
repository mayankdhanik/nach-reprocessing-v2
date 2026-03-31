import React from "react";
import { formatCurrency } from "../utils/helpers";

const StatCard = ({ title, value, subtext, color, bg }) => (
  <div className="card" style={{ padding: "1.25rem" }}>
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
      <div>
        <p style={{ fontSize: "0.75rem", fontWeight: 600, color: "#64748b", textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: "0.375rem" }}>
          {title}
        </p>
        <p style={{ fontSize: "1.75rem", fontWeight: 700, color: "#0f172a", lineHeight: 1 }}>
          {typeof value === "number" ? value.toLocaleString() : value}
        </p>
        <p style={{ fontSize: "0.75rem", color: "#94a3b8", marginTop: "0.375rem" }}>{subtext}</p>
      </div>
      <div style={{ width: 40, height: 40, borderRadius: "0.5rem", background: bg, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
        <div style={{ width: 12, height: 12, borderRadius: "50%", background: color }} />
      </div>
    </div>
  </div>
);

const Dashboard = ({ stats, loading }) => {
  const successRate = stats.total > 0
    ? (((stats.success || 0) + (stats.reprocessed || 0)) / stats.total * 100).toFixed(1)
    : "0.0";

  if (loading) {
    return (
      <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: "1rem" }}>
        {[...Array(5)].map((_, i) => (
          <div key={i} className="card" style={{ padding: "1.25rem" }}>
            <div style={{ height: 12, background: "#f1f5f9", borderRadius: 4, width: "60%", marginBottom: 12 }} />
            <div style={{ height: 28, background: "#f1f5f9", borderRadius: 4, width: "40%", marginBottom: 8 }} />
            <div style={{ height: 10, background: "#f1f5f9", borderRadius: 4, width: "80%" }} />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: "1rem" }}>
        <StatCard
          title="Total"
          value={stats.total || 0}
          subtext={formatCurrency(stats.totalAmount || 0)}
          color="#3b82f6"
          bg="#eff6ff"
        />
        <StatCard
          title="Success"
          value={stats.success || 0}
          subtext={formatCurrency((stats.successAmount || 0) - (stats.reprocessedAmount || 0))}
          color="#10b981"
          bg="#f0fdf4"
        />
        <StatCard
          title="Error / Failed"
          value={(stats.error || 0) + (stats.failed || 0)}
          subtext={formatCurrency(stats.errorAmount || 0)}
          color="#ef4444"
          bg="#fef2f2"
        />
        <StatCard
          title="Stuck"
          value={stats.stuck || 0}
          subtext="Needs attention"
          color="#f59e0b"
          bg="#fffbeb"
        />
        <StatCard
          title="Reprocessed"
          value={stats.reprocessed || 0}
          subtext={formatCurrency(stats.reprocessedAmount || 0)}
          color="#8b5cf6"
          bg="#f5f3ff"
        />
      </div>

      {/* Summary strip */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "1rem", background: "linear-gradient(135deg, #eff6ff, #f5f3ff)", borderRadius: "0.75rem", border: "1px solid #dbeafe", padding: "1rem 1.5rem" }}>
        <div style={{ textAlign: "center" }}>
          <div style={{ fontSize: "1.5rem", fontWeight: 700, color: "#2563eb" }}>{successRate}%</div>
          <div style={{ fontSize: "0.75rem", color: "#64748b", marginTop: 2 }}>Success Rate</div>
        </div>
        <div style={{ textAlign: "center", borderLeft: "1px solid #dbeafe", borderRight: "1px solid #dbeafe" }}>
          <div style={{ fontSize: "1.5rem", fontWeight: 700, color: "#059669" }}>
            {formatCurrency(stats.successAmount || 0)}
          </div>
          <div style={{ fontSize: "0.75rem", color: "#64748b", marginTop: 2 }}>Total Processed</div>
        </div>
        <div style={{ textAlign: "center" }}>
          <div style={{ fontSize: "1.5rem", fontWeight: 700, color: "#dc2626" }}>
            {(stats.error || 0) + (stats.stuck || 0) + (stats.failed || 0)}
          </div>
          <div style={{ fontSize: "0.75rem", color: "#64748b", marginTop: 2 }}>Requiring Action</div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
