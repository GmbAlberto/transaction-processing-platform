import axios from "axios";
import {
    getToken,
    isTokenExpired,
    removeToken,
} from "../utils/tokenUtils";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 10000,
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use(
    (config) => {
        const token = getToken();

        if (!token) {
            return config;
        }

        if (isTokenExpired(token)) {
            removeToken();

            if (window.location.pathname !== "/login") {
                window.location.replace("/login");
            }

            return Promise.reject(new Error("La sesión ha expirado"));
        }

        config.headers.Authorization = `Bearer ${token}`;

        return config;
    },
    (error) => Promise.reject(error),
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;

        if (status === 401 || status === 403) {
            removeToken();

            if (window.location.pathname !== "/login") {
                window.location.replace("/login");
            }
        }

        return Promise.reject(error);
    },
);

export default api;