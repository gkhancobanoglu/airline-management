import axios from "axios";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status;

    if (
      status === 401 &&
      typeof window !== "undefined" &&
      window.location.pathname !== "/login"
    ) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }

    return Promise.reject(err);
  }
);

export default api;
