# RGBradford application

<img src="https://img.shields.io/badge/version-1.0.0-blue.svg"> <img src="https://img.shields.io/badge/java-17%2B-orange.svg?logo=openjdk&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg"> <img src="https://img.shields.io/badge/React-19.1.1-61DAFB.svg?logo=react"> <img src="https://img.shields.io/badge/Tailwind_CSS-4.x-38B2AC.svg?logo=tailwindcss&logoColor=white"> <img src="https://img.shields.io/badge/API-REST-blueviolet.svg">  <img src="https://img.shields.io/badge/docker-supported-2496ED.svg?logo=docker&logoColor=white">

<p>RGBradford is a web-based platform that analyzes microplate photographs and automatically provides protein calculations.</p>

## Quick Start with Docker

Run the entire application (frontend, backend, and database) with a single command:

```bash
docker-compose up --build
```

### Prerequisites

- [Docker](https://www.docker.com/get-started) (with Docker Compose)
- No other configuration needed!

### First Time Setup

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd RGBradford-app
   ```

2. Build and start all services:
   ```bash
   docker-compose up --build
   ```

3. Access the application:
   - **Frontend**: http://localhost:3000
   - **Backend API**: http://localhost:8080
   - **API Documentation**: http://localhost:8080/swagger-ui.html

### Stopping the Application

Press `Ctrl+C` in the terminal where docker-compose is running

Or run:
```bash
docker-compose down
```

### Clearing Database Data

To remove volumes (database data) and start fresh:
```bash
docker-compose down -v
docker-compose up --build
```

### Architecture

**Local Development (Docker Compose):**
- Frontend (React + Vite) served by Nginx on port 3000
- Backend (Spring Boot) on port 8080
- MySQL 8.0 database on port 3306
- Nginx proxies `/api` requests from frontend to backend
- All services communicate via Docker network

**Request Flow:**
```
Browser → http://localhost:3000 → Nginx (Frontend)
                                    ↓
                                  /api/* → Backend (Spring Boot) → MySQL
```

---

### Optional: Custom Environment Variables

The application uses sensible defaults that work out of the box. If you need to customize settings, create a `.env` file from `.env.example`:

```bash
cp .env.example .env
```

**Available configurations:**

**Database Configuration:**
- `MYSQL_DATABASE` - Database name (default: rgbradford_db)
- `MYSQL_USER` - Database user (default: rgbradford_user)
- `MYSQL_PASSWORD` - Database password (default: dev_password)
- `MYSQL_PORT` - MySQL port (default: 3306)

**Application Ports:**
- `FRONTEND_PORT` - Frontend port (default: 3000)
- `BACKEND_PORT` - Backend API port (default: 8080)

**CORS Configuration:**
- `CORS_ALLOWED_ORIGINS` - Allowed origins for CORS (default: http://localhost:5173,http://localhost:3000)

**JWT Configuration:**
- `JWT_SECRET` - Secret key for JWT tokens
