import axios from 'axios';

// Base API URL default to local Spring Boot backend address
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

let inMemoryToken = null;
let logoutCallback = null;

// Store access token in-memory
export const setAccessToken = (token) => {
  inMemoryToken = token;
};

// Register a callback to trigger app-wide logout on refresh failure
export const registerLogoutCallback = (callback) => {
  logoutCallback = callback;
};

// Request Interceptor: Append access token header
api.interceptors.request.use(
  (config) => {
    // If we have an in-memory access token, attach it
    if (inMemoryToken && !config.headers['Authorization']) {
      config.headers['Authorization'] = `Bearer ${inMemoryToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle token refresh on 401 Unauthorized
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Avoid infinite loop if the refresh call itself or login call fails with 401
    const isAuthUrl = originalRequest.url.includes('/auth/login') || originalRequest.url.includes('/auth/refresh');

    if (error.response?.status === 401 && !isAuthUrl && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('artezans_refresh');

      if (refreshToken) {
        try {
          // Perform refresh token exchange (backend expects refresh token in Authorization header)
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {}, {
            headers: {
              'Authorization': `Bearer ${refreshToken}`,
            },
          });

          // Extract new tokens from backend payload
          const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data;

          // Save new tokens
          setAccessToken(newAccessToken);
          localStorage.setItem('artezans_refresh', newRefreshToken);

          // Update header and retry the original request
          originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
          return api(originalRequest);
        } catch (refreshError) {
          console.error('Refresh token failed:', refreshError);
          // Token is invalid/expired; trigger logout
          if (logoutCallback) logoutCallback();
        }
      } else {
        // No refresh token available; trigger logout
        if (logoutCallback) logoutCallback();
      }
    }

    return Promise.reject(error);
  }
);

export default api;
