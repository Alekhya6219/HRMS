
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to every API request
api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('peoplepulse_token');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Handle expired or invalid authentication
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem('peoplepulse_token');
      sessionStorage.removeItem('peoplepulse_user');
    }

    return Promise.reject(error);
  }
);

export default api;
