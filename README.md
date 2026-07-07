# Task Management Service

A Spring Boot microservice for managing tasks in a multi-tenant workforce platform. It exposes a REST API secured with Keycloak JWT tokens and persists data in PostgreSQL.

## Tech Stack

- Java 17
- Spring Boot 4.1
- Spring Data JPA
- Spring Security (OAuth2 Resource Server)
- PostgreSQL 17
- Keycloak (`workforce-os` realm)
- SpringDoc OpenAPI (Swagger UI)
- Docker & Docker Compose

## Features

- CRUD operations for tasks
- Task assignment and self-claiming
- Status workflow: `TODO` → `IN_PROGRESS` → `DONE`
- Priority levels: `LOW`, `MEDIUM`, `HIGH`
- Role-based access control (`ADMIN`, `SUPER_ADMIN`, `USER`)
- Multi-tenant isolation via `enterprise_id` JWT claim
- Interactive API documentation with Swagger UI

## Architecture

The project follows a layered structure:

```
presentation/     → REST controllers, exception handling
application/      → DTOs, services, security helpers
domain/           → entities, repositories (interfaces), business rules
infrastructure/   → JPA adapters, security & tenant configuration
```

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Keycloak running on `http://localhost:9090` with the `workforce-os` realm

## Quick Start (Docker)

From the project root:

```bash
docker compose up -d
```

| Service    | URL / Port                          |
|------------|-------------------------------------|
| API        | http://localhost:8080               |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| PostgreSQL | localhost:5433                      |

Stop the stack:

```bash
docker compose down
```

## Local Development (without Docker)

1. Start PostgreSQL (or run only the database container):

   ```bash
   docker compose up -d db
   ```

2. Make sure Keycloak is running at `http://localhost:9090/realms/workforce-os`.

3. Run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

   The app starts on **port 8081** when run locally (see `application.properties`).

## API Endpoints

All endpoints are under `/api/tasks` and require a valid Bearer token, except Swagger/OpenAPI routes.

| Method  | Endpoint                  | Access                          | Description              |
|---------|---------------------------|---------------------------------|--------------------------|
| `POST`  | `/api/tasks`              | Admin                           | Create a task            |
| `GET`   | `/api/tasks`              | Authenticated                   | List tasks (filtered)    |
| `GET`   | `/api/tasks/{id}`         | Admin or assignee               | Get task by ID           |
| `PUT`   | `/api/tasks/{id}`         | Admin                           | Update task details      |
| `PATCH` | `/api/tasks/{id}/assign`  | Admin                           | Assign task to a user    |
| `PATCH` | `/api/tasks/{id}/claim`   | Authenticated                   | Self-claim unassigned task |
| `PATCH` | `/api/tasks/{id}/status`  | Admin or assignee               | Change task status       |
| `DELETE`| `/api/tasks/{id}`         | Admin                           | Delete a task            |

### Query Parameters (`GET /api/tasks`)

- `status` — `TODO`, `IN_PROGRESS`, `DONE`
- `priority` — `LOW`, `MEDIUM`, `HIGH`

### Example: Create Task

```json
POST /api/tasks
Authorization: Bearer <token>

{
  "title": "Review pull request",
  "description": "Review the latest changes",
  "priority": "HIGH",
  "dueDate": "2026-07-15"
}
```

## Authentication

This service **verifies** JWT tokens issued by Keycloak — it does not handle login itself.

1. Obtain an access token from Keycloak (`workforce-os` realm).
2. Send it in the `Authorization` header: `Bearer <token>`.
3. In Swagger UI, click **Authorize** and paste the token.

Expected JWT claims:

- `preferred_username` — used for task assignment and filtering
- `enterprise_id` — used for multi-tenant data isolation
- `realm_access.roles` — mapped to Spring roles (`ADMIN`, `USER`, etc.)

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8081` | Local dev port (overridden to `8080` in Docker) |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5433/taskdb` | Database connection |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `http://localhost:9090/realms/workforce-os` | Keycloak issuer |

## Running Tests

```bash
./mvnw test
```

Tests use an in-memory H2 database and do not require PostgreSQL or Keycloak.

## Project Structure

```
├── src/main/java/com/example/taskmanagementservice/
│   ├── application/        # Services, DTOs, security
│   ├── domain/             # Domain model & repository interfaces
│   ├── infrastructure/     # JPA, config, filters
│   └── presentation/       # REST controllers
├── src/main/resources/
│   └── application.properties
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```
