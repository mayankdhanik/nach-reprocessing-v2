// Format currency
export const formatCurrency = (amount) => {
  if (!amount && amount !== 0) return "₹0.00";
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    minimumFractionDigits: 2,
  }).format(amount);
};

// Format date
export const formatDate = (dateString) => {
  if (!dateString) return "N/A";
  const date = new Date(dateString);
  return new Intl.DateTimeFormat("en-IN", {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
};

// Format date (short)
export const formatDateShort = (dateString) => {
  if (!dateString) return "N/A";
  const date = new Date(dateString);
  return new Intl.DateTimeFormat("en-IN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(date);
};

// Get status color class
export const getStatusColorClass = (status) => {
  const s = status?.toUpperCase();
  const map = {
    SUCCESS: "status-SUCCESS",
    ERROR: "status-ERROR",
    FAILED: "status-FAILED",
    STUCK: "status-STUCK",
    REPROCESSED: "status-REPROCESSED",
  };
  return map[s] || "status-STUCK";
};

// Get file type badge color
export const getFileTypeBadgeColor = (fileType) => {
  switch (fileType?.toUpperCase()) {
    case "DR":
      return "bg-red-100 text-red-800";
    case "CR":
      return "bg-green-100 text-green-800";
    default:
      return "bg-gray-100 text-gray-800";
  }
};

// Validate file type
export const validateFileType = (file) => {
  const allowedTypes = [".txt"];
  const fileName = file.name.toLowerCase();
  return allowedTypes.some((type) => fileName.endsWith(type));
};

// Validate file size (max 10MB)
export const validateFileSize = (file, maxSizeMB = 10) => {
  const maxSizeBytes = maxSizeMB * 1024 * 1024;
  return file.size <= maxSizeBytes;
};

// Generate transaction reference number
export const generateTxnRef = () => {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 1000);
  return `BDBL${timestamp}${random}`;
};

// Filter transactions
export const filterTransactions = (transactions, filters) => {
  if (!transactions) return [];

  return transactions.filter((txn) => {
    // Status filter
    if (
      filters.status &&
      filters.status !== "ALL" &&
      txn.status !== filters.status
    ) {
      return false;
    }

    // File type filter
    if (
      filters.fileType &&
      filters.fileType !== "ALL" &&
      txn.fileType !== filters.fileType
    ) {
      return false;
    }

    // Search filter
    if (filters.search) {
      const searchTerm = filters.search.toLowerCase();
      const searchFields = [
        txn.txnRefNo,
        txn.mandateId,
        txn.accountNo,
        txn.fileName,
        txn.errorDesc,
      ];

      const matches = searchFields.some(
        (field) => field && field.toString().toLowerCase().includes(searchTerm)
      );

      if (!matches) return false;
    }

    // Date range filter
    if (filters.dateFrom || filters.dateTo) {
      const txnDate = new Date(txn.processedDate);

      if (filters.dateFrom && txnDate < new Date(filters.dateFrom)) {
        return false;
      }

      if (filters.dateTo && txnDate > new Date(filters.dateTo)) {
        return false;
      }
    }

    return true;
  });
};

// Sort transactions
export const sortTransactions = (
  transactions,
  sortField,
  sortDirection = "asc"
) => {
  if (!transactions) return [];

  return [...transactions].sort((a, b) => {
    let aValue = a[sortField];
    let bValue = b[sortField];

    // Handle different data types
    if (typeof aValue === "string") {
      aValue = aValue.toLowerCase();
      bValue = bValue.toLowerCase();
    }

    if (aValue < bValue) {
      return sortDirection === "asc" ? -1 : 1;
    }
    if (aValue > bValue) {
      return sortDirection === "asc" ? 1 : -1;
    }
    return 0;
  });
};

// Calculate statistics
export const calculateStats = (transactions) => {
  if (!transactions || transactions.length === 0) {
    return {
      total: 0,
      success: 0,
      error: 0,
      stuck: 0,
      failed: 0,
      reprocessed: 0,
      totalAmount: 0,
      successAmount: 0,
      errorAmount: 0,
    };
  }

  const stats = {
    total: transactions.length,
    success: 0,
    error: 0,
    stuck: 0,
    failed: 0,
    reprocessed: 0,
    totalAmount: 0,
    successAmount: 0,
    errorAmount: 0,
    reprocessedAmount: 0,
  };

  transactions.forEach((txn) => {
    const amount = parseFloat(txn.amount) || 0;
    stats.totalAmount += amount;

    switch (txn.status?.toUpperCase()) {
      case "SUCCESS":
        stats.success++;
        stats.successAmount += amount;
        break;
      case "ERROR":
        stats.error++;
        stats.errorAmount += amount;
        break;
      case "STUCK":
        stats.stuck++;
        stats.errorAmount += amount;
        break;
      case "FAILED":
        stats.failed++;
        stats.errorAmount += amount;
        break;
      case "REPROCESSED":
        stats.reprocessed++;
        stats.reprocessedAmount += amount;
        stats.successAmount += amount;
        break;
      default:
        break;
    }
  });

  return stats;
};

// Debounce function for search
export const debounce = (func, wait) => {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

// Copy to clipboard
export const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch (err) {
    console.error("Failed to copy text: ", err);
    return false;
  }
};

// Download data as CSV
export const downloadCSV = (data, filename = "nach-transactions.csv") => {
  if (!data || data.length === 0) return;

  const headers = Object.keys(data[0]);
  const csvContent = [
    headers.join(","),
    ...data.map((row) =>
      headers.map((header) => JSON.stringify(row[header] || "")).join(",")
    ),
  ].join("\n");

  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
  const link = document.createElement("a");

  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute("download", filename);
    link.style.visibility = "hidden";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
};

// Show notification (you can integrate with a toast library)
export const showNotification = (message, type = "info") => {
  // Simple console notification for now
  // You can integrate with react-toastify or similar library
  console.log(`${type.toUpperCase()}: ${message}`);

  // Basic browser notification
  if (Notification.permission === "granted") {
    new Notification(
      `NACH System - ${type.charAt(0).toUpperCase() + type.slice(1)}`,
      {
        body: message,
        icon: "/favicon.ico",
      }
    );
  }
};

// Request notification permission
export const requestNotificationPermission = () => {
  if ("Notification" in window && Notification.permission === "default") {
    Notification.requestPermission();
  }
};
