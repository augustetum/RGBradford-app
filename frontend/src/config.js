// API Configuration
// Priority:
// 1. VITE_API_URL env var (set on Render or in .env file)
// 2. Development mode: use localhost:8080
// 3. Production mode without VITE_API_URL: use /api (for local docker-compose with nginx proxy)
const API_BASE_URL = import.meta.env.VITE_API_URL
  ? `${import.meta.env.VITE_API_URL}/api`
  : import.meta.env.MODE === 'production'
    ? '/api'  // This will be proxied by Nginx to the backend in local docker-compose
    : 'http://localhost:8080/api';

export { API_BASE_URL };