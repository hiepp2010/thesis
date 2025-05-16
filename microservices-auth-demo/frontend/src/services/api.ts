import axios from 'axios';

// Create an axios instance with default config
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  // We don't need withCredentials since we're using JWT tokens
  withCredentials: false
});

// API for authentication
export const authApi = {
  async register(username: string, email: string, password: string) {
    try {
      const response = await api.post('/api/auth/register', { username, email, password });
      return response;
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  },
  
  async login(username: string, password: string) {
    try {
      const response = await api.post('/api/auth/authenticate', { username, password });
      return response;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  },
  
  getStoredToken() {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('token');
    }
    return null;
  },
  
  getStoredUser() {
    if (typeof window !== 'undefined') {
      const user = localStorage.getItem('user');
      return user ? JSON.parse(user) : null;
    }
    return null;
  },
  
  storeUserData(token: string, user: any) {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
  },
  
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }
};

// Intercept requests to add the auth token when available
api.interceptors.request.use(config => {
  // Add Authorization header if token exists
  const token = authApi.getStoredToken();
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  
  return config;
});

export default api; 