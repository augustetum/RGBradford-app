# RGBradford Backend (RGBradford-app)

## Overview

RGBradford is a backend service for protein quantitation workflows based on the Bradford assay. It provides APIs to:

- Manage projects, plate layouts, wells, and standards
- Upload and analyze plate images to compute optical densities and concentrations
- Generate calibration curves and export results
- Secure endpoints with JWT-based authentication

This repository contains the Spring Boot backend that powers the analysis and data management layers. It exposes a REST API documented with OpenAPI/Swagger and is designed to run locally (MySQL by default) and in production (e.g., Azure SQL) with Docker.

## Quick Links

- API Reference (Swagger UI): `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Endpoints summary: [`API_ENDPOINTS_SUMMARY.md`](./API_ENDPOINTS_SUMMARY.md)
- Project docs (PDF): [docs/RGBradford-Documentation.pdf](./docs/RGBradford-Documentation.pdf) (placeholder)

## Showcase Video

Add a short demo video here after you record it. Suggested placeholder:

```
![RGBradford Demo](./docs/showcase-thumbnail.png)

[Watch the Showcase Video](https://your-video-link.example.com)
```

You can replace the thumbnail image and the link once the video is ready.

---

## Tech Stack

- Spring Boot 3 (Java 17)
- Spring Web, Spring Security (JWT), Spring Data JPA
- Databases: MySQL (dev default), Azure SQL (example prod), H2 (runtime dep available)
- OpenAPI via `springdoc-openapi`
- Apache Commons Math (curve fitting)
- Maven build, Docker containerization

Key configuration files:

- `pom.xml`
- `src/main/resources/application.properties`
- `Dockerfile`
- `docker-compose.yml`

---

## Getting Started (Local)

Prerequisites:

- Java 17+
- Maven 3.9+
- MySQL 8.x (for default dev setup)

### 1) Configure Database (Dev)

By default, the backend uses MySQL with the following defaults (see `src/main/resources/application.properties`):

```
spring.datasource.url=jdbc:mysql://localhost:3306/rgbradford_db
spring.datasource.username=rgbradford_user
spring.datasource.password=dev_password
```

Create a local database and user (example):

```sql
CREATE DATABASE rgbradford_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'rgbradford_user'@'%' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON rgbradford_db.* TO 'rgbradford_user'@'%';
FLUSH PRIVILEGES;
```

Alternatively, you can override these with environment variables (see Environment Variables below).

### 2) Run the App

Option A: Maven (recommended for development)

```bash
mvn spring-boot:run
```

Option B: Build the JAR and run

```bash
mvn clean package -DskipTests
java -jar target/*.jar
```

The application starts on `http://localhost:8080`.

### 3) Explore the API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

---

## Running with Docker

### Build image

```bash
docker build -t rgbradford-backend .
```

### Run container

Pass the appropriate env vars for your database and JWT secret:

```bash
docker run \
  -p 8080:8080 \
  -e DATABASE_URL="jdbc:mysql://host.docker.internal:3306/rgbradford_db" \
  -e DATABASE_USERNAME="rgbradford_user" \
  -e DATABASE_PASSWORD="dev_password" \
  -e JWT_SECRET="change-me-in-prod" \
  rgbradford-backend
```

### Docker Compose

`docker-compose.yml` exposes port 8080 and supports many env vars for production-like deployments (e.g., Azure SQL):

```bash
docker compose up -d
```

By default, the compose file maps 8080:8080 and uses `SPRING_PROFILES_ACTIVE=prod` if not provided. Override variables as needed.

---

## Environment Variables

These map to `src/main/resources/application.properties` and `docker-compose.yml` and can override defaults:

- DATABASE_URL (e.g., `jdbc:mysql://localhost:3306/rgbradford_db` or Azure SQL JDBC URL)
- DATABASE_USERNAME
- DATABASE_PASSWORD
- DATABASE_DRIVER (default dev: `com.mysql.cj.jdbc.Driver`, prod example: `com.microsoft.sqlserver.jdbc.SQLServerDriver`)
- JPA_DIALECT (dev default: `org.hibernate.dialect.MySQLDialect`, prod example: `org.hibernate.dialect.SQLServerDialect`)
- JPA_DDL_AUTO (dev default: `update`, prod recommended: `validate`)
- JPA_SHOW_SQL (dev default: `true`)
- PORT (default: `8080`)
- JWT_SECRET (dev default in properties; must set a strong secret in prod)
- LOG_LEVEL / SPRING_LOG_LEVEL / APP_LOG_LEVEL
- CORS_ORIGINS (default: `*`)
- MAX_FILE_SIZE / MAX_REQUEST_SIZE (multipart limits)

Notes:

- Swagger paths are configured as `/swagger-ui.html` and `/api-docs`.
- Logging for web and springdoc is enabled at DEBUG in dev (see `application.properties`).

---

## Security & Authentication

- JWT-based authentication is enabled via Spring Security. Configure your `JWT_SECRET` in env vars.
- Clients should include an `Authorization: Bearer <token>` header when calling protected endpoints.
- OAuth2 client/resource-server dependencies are present; if you integrate external identity providers, document the flow and required env vars accordingly.

---

## Core Features

- Plate layout CRUD and well management
- Analysis of plate images with configurable parameters
- Calibration curve generation (Apache Commons Math)
- CSV export of results
- Pagination, filtering, and robust error handling

For endpoint-level details, see:

- [`API_ENDPOINTS_SUMMARY.md`](./API_ENDPOINTS_SUMMARY.md)
- Swagger UI at runtime

---

## Project Structure

- `src/main/java/com/rgbradford/backend/` — Spring Boot application code (controllers, services, entities, etc.)
- `src/main/resources/application.properties` — default configuration for dev
- `pom.xml` — Maven configuration and dependencies
- `Dockerfile` — multi-stage Docker build
- `docker-compose.yml` — container config and environment variables
- `API_ENDPOINTS_SUMMARY.md` — quick summary of key API endpoints

Entry point: `com.rgbradford.backend.RgBradfordBackendApplication`.

---

## Building & Testing

Build:

```bash
mvn clean package
```

Run tests:

```bash
mvn test
```

Dev hot reload:

- `spring-boot-devtools` is included; use your IDE’s Spring Boot dev cycle for rapid iteration.

---

## Troubleshooting

- Port already in use: change `PORT` or free 8080.
- Database connection errors: verify `DATABASE_URL`, credentials, network reachability, and driver.
- Schema issues on startup: adjust `JPA_DDL_AUTO` (`update` for dev, `validate` for prod) and `JPA_DIALECT`.
- Swagger not reachable: confirm the app is on port 8080 and visit `/swagger-ui.html`.

---

## Roadmap Ideas

- Role-based access control (RBAC) policies
- Advanced analysis visualizations and reports
- Import/export of plate layouts in standardized formats
- Integration with external ELN/LIMS systems

---

## Documentation

- API Reference: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Comprehensive project documentation (PDF): place your file at `docs/RGBradford-Documentation.pdf` and update links as needed.

---

## Contributing

Contributions are welcome!

- Open an issue describing the change or bug.
- For PRs, include context, testing notes, and screenshots for UI-facing changes (if applicable).

---

## License

See [`LICENSE`](./LICENSE) for licensing information.