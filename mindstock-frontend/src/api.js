const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";
const VISION_URL = import.meta.env.VITE_VISION_URL || "http://localhost:8081";

async function request(url, endpoint, options = {}) {
  const token = localStorage.getItem("ms_token");
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (token) headers["Authorization"] = "Bearer " + token;

  const response = await fetch(`${url}${endpoint}`, { ...options, headers });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: "Error desconocido" }));
    throw new Error(error.error || error.detail || `HTTP ${response.status}`);
  }
  return response.json();
}

const api = (endpoint, options) => request(BASE_URL, endpoint, options);
const vision = (endpoint, options) => request(VISION_URL, endpoint, options);

export const authAPI = {
  login: (email, password) =>
    api("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) }),
  register: (data) =>
    api("/auth/register", { method: "POST", body: JSON.stringify(data) }),
  loginRfid: (rfidUid) =>
    api("/auth/rfid", { method: "POST", body: JSON.stringify({ rfidUid }) }),
};

export const itemsAPI = {
  getAll: () => api("/items"),
  getById: (id) => api(`/items/${id}`),
  getDisponibles: () => api("/items/disponibles"),
  getByCategory: (categoryId) => api(`/items/category/${categoryId}`),
  search: (query) => api(`/items/search?q=${encodeURIComponent(query)}`),
  getStats: () => api("/items/stats"),
};

export const usersAPI = {
  getAll: () => api("/users"),
  getById: (id) => api(`/users/${id}`),
  getByRol: (rol) => api(`/users/rol/${rol}`),
};

export const loansAPI = {
  createRequest: (data) =>
    api("/loans/requests", { method: "POST", body: JSON.stringify(data) }),
  getPendingRequests: () => api("/loans/requests/pending"),
  approveRequest: (requestId, laboratoristaId) =>
    api(`/loans/requests/${requestId}/approve?laboratoristaId=${laboratoristaId}`, { method: "PUT" }),
  rejectRequest: (requestId) =>
    api(`/loans/requests/${requestId}/reject`, { method: "PUT" }),
  getActiveLoans: () => api("/loans/active"),
  getByUser: (userId) => api(`/loans/user/${userId}`),
  returnLoan: (loanId) => api(`/loans/${loanId}/return`, { method: "PUT" }),
};

export const categoriesAPI = { getAll: () => api("/categories") };
export const labsAPI = { getAll: () => api("/labs") };

export const visionAPI = {
  getStatus: () => vision("/"),
  simulateDetection: () => vision("/detect/simulate", { method: "POST" }),
  confirmLoan: (data) =>
    vision("/detect/confirm-loan", { method: "POST", body: JSON.stringify(data) }),
  detectFrame: async (blob) => {
    const formData = new FormData();
    formData.append("file", blob, "frame.jpg");
    const r = await fetch(`${VISION_URL}/detect/frame`, { method: "POST", body: formData });
    if (!r.ok) {
      const e = await r.json().catch(() => ({ detail: "Error" }));
      throw new Error(e.detail || `HTTP ${r.status}`);
    }
    return r.json();
  },
};
