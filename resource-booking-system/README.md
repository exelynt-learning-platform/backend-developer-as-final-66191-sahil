# Resource Booking System

A RESTful **Resource Booking System** built with **Spring Boot 3 / Java 17**, securing access with **Spring Security + JWT** and persisting data with **JPA/Hibernate** on **PostgreSQL or MySQL**.

Users can browse available resources (rooms, vehicles, equipment) and manage their own reservations. Administrators have full CRUD access over both resources and all reservations.

---

## 1. Tech Stack

| Concern            | Choice                                             |
|---------------------|-----------------------------------------------------|
| Language / Runtime  | Java 17+                                            |
| Framework           | Spring Boot 3.3.x (Web, Data JPA, Security, Validation) |
| Auth                | Stateless JWT (jjwt 0.12.x), BCrypt password hashing |
| Database            | PostgreSQL **or** MySQL (both drivers bundled)      |
| API Docs            | springdoc-openapi (Swagger UI)                      |
| Build               | Maven                                               |

---

## 2. Project Structure

```
src/main/java/com/bookingsystem/
├── BookingSystemApplication.java
├── config/          # Security, OpenAPI, seed data
├── security/         # JwtService, JwtAuthFilter, UserDetailsServiceImpl
├── entity/            # User, Resource, Reservation, Role, ReservationStatus
├── repository/       # Spring Data JPA repositories + Specifications
├── dto/               # Request/response records, grouped by feature
├── controller/        # AuthController, ResourceController, ReservationController
├── service/           # AuthService, ResourceService, ReservationService
└── exception/         # Custom exceptions + GlobalExceptionHandler
```

---

## 3. Prerequisites

- Java 17+
- Maven 3.8+
- A running PostgreSQL **or** MySQL instance

---

## 4. Configuration (Environment Variables)

Copy `.env.example` to `.env` (or export these variables in your shell / CI) and adjust as needed:

| Variable                | Default (postgres profile)                                   | Description                                       |
|--------------------------|---------------------------------------------------------------|----------------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | `postgres`                                                    | `postgres` or `mysql`                              |
| `DB_HOST`                | `localhost`                                                   | Database host                                      |
| `DB_PORT`                | `5432` (postgres) / `3306` (mysql)                            | Database port                                      |
| `DB_NAME`                | `booking_system`                                              | Database/schema name                               |
| `DB_USERNAME`            | `postgres` / `root`                                           | Database user                                      |
| `DB_PASSWORD`            | `postgres` / `root`                                           | Database password                                  |
| `JPA_DDL_AUTO`           | `update`                                                      | Hibernate schema strategy (`update`, `validate`, `none`, ...) |
| `JPA_SHOW_SQL`           | `false`                                                       | Log generated SQL                                  |
| `JWT_SECRET`             | (dev-only default baked in, **override in prod**)            | Base64-encoded HMAC secret, must decode to ≥256 bits |
| `JWT_EXPIRATION_MS`      | `86400000` (24h)                                              | Token lifetime in milliseconds                     |
| `SERVER_PORT`            | `8080`                                                        | HTTP port                                          |
| `CORS_ALLOWED_ORIGINS`   | `http://localhost:3000`                                       | Comma-separated browser origins allowed to call the API |

Generate a secure JWT secret with:
```bash
openssl rand -base64 32
```

---

## 5. Database Setup

### Option A — PostgreSQL (default)
```bash
createdb booking_system
# or: psql -c "CREATE DATABASE booking_system;"
export SPRING_PROFILES_ACTIVE=postgres
export DB_HOST=localhost DB_PORT=5432 DB_NAME=booking_system DB_USERNAME=postgres DB_PASSWORD=postgres
```

### Option B — MySQL
```bash
mysql -u root -p -e "CREATE DATABASE booking_system;"
export SPRING_PROFILES_ACTIVE=mysql
export DB_HOST=localhost DB_PORT=3306 DB_NAME=booking_system DB_USERNAME=root DB_PASSWORD=root
```

Hibernate (`JPA_DDL_AUTO=update`) will create/update all tables automatically on startup — no manual migration scripts are required for local development.

---

## 6. Running the Application

```bash
mvn clean install
mvn spring-boot:run
```

Or run the packaged jar:
```bash
mvn clean package -DskipTests
java -jar target/resource-booking-system-1.0.0.jar
```

The app starts on `http://localhost:8080` by default.

### Seed Users

On first startup, `DataSeeder` creates the following accounts if they don't already exist:

| Username | Password    | Role  |
|----------|-------------|-------|
| `admin`  | `Admin@123` | ADMIN |
| `user`   | `User@123`  | USER  |
| `user2`  | `User@123`  | USER  |

It also seeds a handful of sample resources (two rooms, a vehicle, a projector) so the API is immediately usable.

---

## 7. API Documentation

Once the app is running:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Postman collection:** `postman_collection.json` (in the project root) — import into Postman; it includes a `baseUrl` variable and auto-captures the JWT into `adminToken` / `userToken` collection variables after login.

All endpoints except `/auth/login`, `/swagger-ui/**` and `/v3/api-docs/**` require a `Authorization: Bearer <token>` header.

---

## 8. Authentication

### `POST /auth/login`
```json
{
  "username": "admin",
  "password": "Admin@123"
}
```
Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ADMIN",
  "expiresInMs": 86400000
}
```
Use the token on subsequent requests: `Authorization: Bearer <token>`.

The username/role embedded in the JWT is what the server trusts for **all** authorization decisions — a reservation's owner is always resolved from the token's subject, never from any field the client sends in the request body.

---

## 9. Authorization Rules (RBAC)

| Action                                    | USER                        | ADMIN |
|--------------------------------------------|------------------------------|-------|
| `GET /api/resources`, `GET /api/resources/{id}` | ✅                            | ✅    |
| `POST/PUT/DELETE /api/resources/**`        | ❌ (403)                      | ✅    |
| `POST /api/reservations`                   | ✅ (creates for **self** only) | ✅    |
| `GET /api/reservations`                    | ✅ (own reservations only)     | ✅ (all reservations) |
| `GET /api/reservations/{id}`               | ✅ (own only, else 403)        | ✅    |
| `PUT /api/reservations/{id}` (full update) | ❌                            | ✅    |
| `PUT /api/reservations/{id}/status`        | ❌                            | ✅    |
| `PUT /api/reservations/{id}/cancel`        | ✅ (own only)                  | ✅    |
| `DELETE /api/reservations/{id}`            | ❌                            | ✅    |

---

## 10. Resources API

### `GET /api/resources`
Query params: `type` (e.g. `ROOM`, `VEHICLE`, `EQUIPMENT`), `available` (`true`/`false`), `page`, `size`, `sort` (e.g. `sort=name,asc`).

### `POST /api/resources` (ADMIN)
```json
{
  "name": "Conference Room C",
  "type": "ROOM",
  "description": "Standup room",
  "location": "Floor 1",
  "capacity": 6,
  "available": true
}
```

### `PUT /api/resources/{id}` (ADMIN) — same body as create.
### `DELETE /api/resources/{id}` (ADMIN)

---

## 11. Reservations API

### `POST /api/reservations`
```json
{
  "resourceId": 1,
  "startTime": "2026-09-01T10:00:00",
  "endTime": "2026-09-01T12:00:00",
  "price": 49.99,
  "notes": "Team sync"
}
```
- The reservation's `userId` is **derived from the JWT**, not accepted from the body.
- Server validates `endTime > startTime`, that the resource is `available`, and that the resource has no overlapping non-cancelled reservation for that window (double-booking returns `409 Conflict`).
- New reservations always start in `PENDING` status.

### `GET /api/reservations`
Query params:
- `status` — `PENDING` | `CONFIRMED` | `CANCELLED`
- `minPrice`, `maxPrice` — decimal bounds
- `page`, `size` — pagination (Spring's standard `Pageable`)
- `sort` — e.g. `sort=price,desc&sort=startTime,asc`

ADMIN gets results across **all** users; USER's results are always scoped to their own reservations, regardless of filters supplied.

### `PUT /api/reservations/{id}/status` (ADMIN)
```json
{ "status": "CONFIRMED" }
```

### `PUT /api/reservations/{id}/cancel`
Owner or ADMIN. Sets status to `CANCELLED`. Returns `400` if already cancelled.

### `DELETE /api/reservations/{id}` (ADMIN)
Permanently removes the reservation.

---

## 12. Error Responses

All errors follow a consistent shape:
```json
{
  "timestamp": "2026-08-26T10:15:30Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields are invalid",
  "path": "/api/reservations",
  "details": ["price: must not be negative"]
}
```

| Status | Scenario                                              |
|--------|----------------------------------------------------------|
| 400    | Bean validation failures, bad date ranges, unavailable resource |
| 401    | Missing/invalid/expired JWT, bad login credentials        |
| 403    | Authenticated but lacking the required role/ownership      |
| 404    | Resource or reservation not found                         |
| 409    | Overlapping reservation for the same resource/time window  |
| 500    | Unexpected server error                                    |

---

## 13. Running Tests

Tests run against an in-memory H2 database (see `src/test/resources/application.yml`) so no external DB is needed:
```bash
mvn test
```

---

## 14. Security Notes for Production

- Override `JWT_SECRET` with a strong, randomly generated Base64 secret (never use the bundled dev default).
- Set `JPA_DDL_AUTO=validate` and manage schema changes with a migration tool (Flyway/Liquibase) once the schema stabilizes.
- Put the app behind HTTPS/TLS; JWTs are bearer tokens and must not travel over plain HTTP.
- Restrict the CORS configuration (`SecurityConfig.corsConfigurationSource`) to your actual frontend origin(s) instead of `*`.
