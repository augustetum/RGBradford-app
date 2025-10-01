// API Configuration
// In Docker/production build, use relative path /api (proxied by Nginx)
// In development (Vite dev server), use localhost:8080
const API_BASE_URL = import.meta.env.MODE === 'production'
  ? '/api'  // This will be proxied by Nginx to the backend
  : import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export { API_BASE_URL };