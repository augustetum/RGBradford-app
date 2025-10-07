# RGBradford application

<img src="https://img.shields.io/badge/version-1.0.0-blue.svg"> <img src="https://img.shields.io/badge/java-17%2B-orange.svg?logo=openjdk&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg"> <img src="https://img.shields.io/badge/React-19.1.1-61DAFB.svg?logo=react"> <img src="https://img.shields.io/badge/Tailwind_CSS-4.x-38B2AC.svg?logo=tailwindcss&logoColor=white"> <img src="https://img.shields.io/badge/API-REST-blueviolet.svg">  <img src="https://img.shields.io/badge/docker-supported-2496ED.svg?logo=docker&logoColor=white">

<p>RGBradford is a software tool that analyzes microplate images and automatically generates a calibration curve and calculates protein concentrations.</p>

<p>The documentation for this project can be found here: <a href="https://2025.igem.wiki/vilnius-lithuania/software/">Software Documentation</a></p>

## Installation
<p>The tool is easily installable with Docker, if you want to install the system locally. Otherwise, the <b>tool is accessible via our <a href>web client.</a></b></p>

### Prerequisites

- [Docker](https://www.docker.com/get-started) (with Docker Compose)
- [Git](https://git-scm.com/downloads) (to clone the repository)
- No other configuration needed.

### First Time Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/augustetum/RGBradford-app.git
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

---
<details>
<summary> <h3>Optional: Custom Environment Variables </h3></summary>

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

</details>

## Usage

### Instructions using web client
After registration (in the web client or locally), start by creating a new project and continue by uploading your Bradford assay picture. The next steps will be explained to you along the way in the application itself.

### API endpoints
An extensive API endpoint library was prepared for this project, the documentation and schemas can be accessed in an OpenAPI format via SwaggerUI: <a href="https://rgbradford-backend.onrender.com/swagger-ui/index.html">https://rgbradford-backend.onrender.com/swagger-ui/index.html</a> or locally. You are welcome to use the library to integrate this project with existing scientific software.


## Contributing
### Limitations and suggestions
<p>Vilnius-Lithuania iGEM 2025 team gladly welcomes any contributions towards the tool’s further development. The limitations and future suggestions of features to be implemented in the project are described <a href="https://2025.igem.wiki/vilnius-lithuania/software/">here</a>, however suggestions that are not listed in the documentation are appreciated.</p>
<p>Please refer to the current version of the software tool in the repository, where key feature updates are described in each release and some proposed improvements may have already been implemented.</p>

### Instructions
<OL>
   <li>Fork the repository</li>
   <li>Commit your changes</li>
   <li>Document your code and provide a description in the pull request</li>
   <li>Contact us to review the code and implement the changes</li>
</OL>




## Authors and acknowledgment
<p>The authors of this project are:</p>
<table>
<thead>
<tr>
<th>Responsibilities</th>
<th>Name, surname</th>
</tr>
</thead>
<tbody>
<tr>
<td>System engineering, backend development, UI design,  documentation</td>
<td>Augustė Tumaitė (<a href="https://github.com/augustetum">https://github.com/augustetum</a>)</td>
</tr>
<tr>
<td>Frontend development, UI design</td>
<td>Augustas Rinkevičius (<a href="https://github.com/augustusthebirb">https://github.com/augustusthebirb</a>)</td>
</tr>
</tbody>
</table>

