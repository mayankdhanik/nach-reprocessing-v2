import axios from "axios";

const api = axios.create({
  baseURL: "/api",
  timeout: 30000,
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    console.error("API error:", err.response?.status, err.response?.data || err.message);
    return Promise.reject(err);
  }
);

export const nachAPI = {
  getUploadedFiles: () => api.get("/nach/transactions?files=true"),
  getTransactionsByFile: (fileName) => api.get(`/nach/transactions?fileName=${encodeURIComponent(fileName)}`),
  getAllTransactions: () => api.get("/nach/transactions"),
  uploadFile: (file) => {
    const formData = new FormData();
    formData.append("file", file);
    return api.post("/nach/upload", formData, { headers: { "Content-Type": "multipart/form-data" } });
  },
  reprocessTransactions: (transactionIds) => api.post("/nach/reprocess", { transactionIds }),
};

export default api;
