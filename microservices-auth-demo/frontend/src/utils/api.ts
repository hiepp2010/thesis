import axios from 'axios';
import { getToken } from './auth';

// Create an API client instance
const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  withCredentials: false  // Important for CORS with wildcard (*) origin
});

// Add a request interceptor to include auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add a response interceptor to handle common errors
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API request error:', error);
    // You can add global error handling here
    return Promise.reject(error);
  }
);

export default apiClient; 