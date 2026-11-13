# E-Commerce Platform

A scalable, secure, and cloud-ready e-commerce platform built using Java 21, Spring Boot, Spring Cloud Gateway, and a microservices architecture.

The project demonstrates production-oriented backend engineering practices, including centralized dependency management, JWT-based authentication, refresh-token management, database migrations, API gateway security, automated testing, and cloud-native development.

> **Project status:** Active development. The Maven multi-module foundation, Auth Service, API Gateway, Product Catalog Service, and Inventory Service are implemented. Auth Service registration, login, refresh-token functionality, logout, and protected-user functionality are available. The API Gateway routes authentication requests to Auth Service and validates JWTs reactively. Four Gateway JWT unit tests have passed. Registration and login through the Gateway, plus valid/unauthorized protected-request scenarios, have also been manually verified.

## Key Features

- **JWT authentication:** Token-based authentication for securing APIs.
- **User registration and login:** Authentication endpoints for user access.
- **Refresh-token management:** Persistent refresh tokens with expiration handling.
- **Logout endpoint:** Logout functionality is implemented; dedicated integration coverage for revocation is still planned.
- **API Gateway:** Spring Cloud Gateway Server WebFlux routes authentication, product, and inventory requests to their services.
- **Inventory Service:** Maintains per-product on-hand stock, reserves quantities, and supports idempotent adjustments, commits, and releases.
- **Reactive JWT validation:** Gateway validates JWT signatures and expiration and establishes authenticated user identity and role authorities.
- **Protected APIs:** Spring Security integration with JWT authentication filters in Auth Service and reactive authentication at the Gateway.
- **Global exception handling:** Authentication and request errors are mapped to HTTP responses.
- **Database versioning:** Flyway migrations manage database schema changes.
- **Centralized dependency management:** Maven parent project and internal BOM support consistent dependency versions.
- **Shared components:** A common module for reusable application components.
- **Automated testing:** JWT unit tests, Gateway JWT validation tests, and PostgreSQL-backed Auth Service integration tests.
- **Cloud-ready architecture:** Designed to support containerization and deployment to cloud infrastructure.

## Technology Stack

| Category | Technologies |
|---|---|
| Language | Java 21 |
| Backend | Spring Boot 3.5.6, Spring Security |
| API Gateway | Spring Cloud Gateway Server WebFlux 4.3.0, Spring WebFlux, Project Reactor |
| Architecture | Maven multi-module microservices |
| Authentication | JWT access tokens, refresh tokens, HS256 |
| Database | PostgreSQL |
| Persistence | Spring Data JPA, Hibernate |
| Database migrations | Flyway |
| Build | Maven |
| Testing | JUnit 5, Spring Boot Test, MockMvc, Reactor Test, Testcontainers 2.0.5 |
| API security | Spring Security, JJWT 0.12.6, reactive JWT authentication |
| Planned messaging | Apache Kafka |
| Planned caching | Redis |
| Planned deployment | Docker, Kubernetes |
| Planned frontend | Angular |

Technologies marked as planned will be integrated as development progresses.

## Project Structure

```text
ecommerce-platform/
├── api-gateway/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/product/ecommerce/gateway/
│   │   │   │   ├── config/
│   │   │   │   └── filter/
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   │       └── java/com/product/ecommerce/gateway/filter/
│   └── pom.xml
├── auth-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/product/ecommerce/auth/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── exception/
│   │   │   │   ├── security/
│   │   │   │   ├── service/
│   │   │   │   ├── token/
│   │   │   │   └── user/
│   │   │   └── resources/
│   │   │       └── db/migration/
│   │   └── test/
│   │       └── java/com/product/ecommerce/auth/
│   └── pom.xml
├── common/
│   └── pom.xml
├── ecommerce-bom/
│   └── pom.xml
├── pom.xml
└── README.md
```

The structure above illustrates the current project organization. Additional directories and modules may be added as development progresses.

### Core Components

**API Gateway**

Provides the external entry point for routed API requests. It currently routes `/api/v1/auth/**` requests to Auth Service and validates bearer JWTs using Spring Security WebFlux before forwarding protected requests.

**Auth Service**

Responsible for user registration, authentication, JWT issuance, refresh-token management, logout, and protected current-user access.

**Common Module**

Provides reusable components shared across application modules.

**E-Commerce BOM**

Centralizes dependency version management to improve consistency and simplify dependency upgrades.

## Authentication and Gateway Flow

The current request flow uses the API Gateway as the entry point for client authentication requests.

```text
Client / Postman
       |
       | HTTP request
       v
API Gateway :8090
       |
       | Route: /api/v1/auth/**
       | Reactive JWT validation for protected requests
       v
Auth Service :8081
       |
       | Authentication and business logic
       v
PostgreSQL :5432
```

The login flow is:

```text
Client
  |
  | 1. Submit login credentials through Gateway
  v
API Gateway :8090
  |
  | 2. Forward public login request
  v
Auth Service :8081
  |
  | 3. Validate credentials
  | 4. Generate access token and issue refresh token
  v
Client
  |
  | 5. Send access token as Bearer token
  v
API Gateway :8090
  |
  | 6. Verify JWT signature and expiration
  | 7. Establish reactive authentication and authorities
  v
Auth Service / Protected API
```

The Gateway and Auth Service must use the same JWT signing secret for the current HS256 implementation. Keep the secret outside source control and configure it independently in each application's runtime environment.

When the access token expires, the client can use the refresh token with the Auth Service refresh flow. Refresh-token rotation and logout revocation still require dedicated automated integration coverage.

## Local Runtime Ports

| Component | Default local port | Purpose |
|---|---:|---|
| PostgreSQL | 5432 | Auth Service persistence |
| Auth Service | 8081 | Authentication endpoints and protected user endpoint |
| API Gateway | 8090 | Client-facing gateway and route forwarding |
| Product Catalog Service | 8082 | Product catalog APIs |
| Inventory Service | 8083 | Stock and reservation APIs |

The API Gateway health endpoint is available at:

```text
http://localhost:8090/actuator/health
```

The current Gateway route forwards:

```text
/api/v1/auth/**
```

to the Auth Service at `http://localhost:8081` by default. These values can be overridden using `AUTH_SERVICE_URL` and `SERVER_PORT`.

## Getting Started

### Prerequisites

Install the following software:

- JDK 21
- Git
- Maven (or the Maven Wrapper, if available)
- PostgreSQL for local application execution
- Docker Desktop or another supported Docker environment for Testcontainers integration tests
- An IDE such as IntelliJ IDEA, Eclipse, Spring Tool Suite, or VS Code

### 1. Clone the Repository

```bash
git clone https://github.com/gyanapatra/ecommerce-platform.git
cd ecommerce-platform
```

### 2. Configure PostgreSQL

Create the authentication database and a dedicated database user.

Example SQL:

```sql
CREATE USER ecommerce_user WITH PASSWORD 'change-me';
CREATE DATABASE ecommerce_auth OWNER ecommerce_user;
```

Replace the example password with a secure local password.

### 3. Configure Environment Variables

Configure the environment variables required by `auth-service/src/main/resources/application.yml`.

Typical Auth Service settings include:

```text
DB_URL=jdbc:postgresql://localhost:5432/ecommerce_auth
DB_USERNAME=ecommerce_user
DB_PASSWORD=your-local-database-password
JWT_SECRET=your-base64-encoded-secret
```

The API Gateway also requires the same `JWT_SECRET` value. Its `application.yml` maps that environment variable to `app.jwt.secret`. The Gateway can additionally use:

```text
AUTH_SERVICE_URL=http://localhost:8081
SERVER_PORT=8090
```

Generate a cryptographically secure, Base64-encoded secret containing at least 32 bytes before Base64 encoding for the current HS256 configuration. Configure the same value for Auth Service and API Gateway. Do not print secrets in shared logs or screenshots.

Use the exact property names and formats expected by the application configuration.

**Security:** Never commit real database passwords, JWT signing secrets, access tokens, or refresh tokens to Git.

### 4. Build and Test the Project

From the repository root:

```bash
mvn clean verify
```

The Auth Service integration tests use PostgreSQL through Testcontainers. Testcontainers requires a working Docker environment and may download the configured PostgreSQL image on the first run.

To run the API Gateway unit tests independently:

```bash
mvn test -pl api-gateway
```

If the repository includes a Maven Wrapper, you can use:

Windows:

```powershell
.\mvnw.cmd clean verify
```

Linux/macOS:

```bash
./mvnw clean verify
```

### 5. Start the Auth Service

Run the application from your IDE or use Maven from the repository root:

```bash
mvn spring-boot:run -pl auth-service
```

Ensure PostgreSQL is running and the required Auth Service environment variables are configured before starting the application. The default application port is 8081.

### 6. Start the API Gateway

In a separate IDE run configuration or terminal, configure `JWT_SECRET` with the same value used by Auth Service, then run:

```bash
mvn spring-boot:run -pl api-gateway
```

The default Gateway port is 8090. Keep both Auth Service and API Gateway running.

## Product Catalog Service

The catalog service owns product data and its Flyway-managed PostgreSQL schema. Create the `ecommerce_product` database, configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, then start it with `mvn spring-boot:run -pl product-service` (default port `8082`). The Gateway forwards `/api/v1/products/**` to it; configure `PRODUCT_SERVICE_URL` to override the local URL. Catalog requests through the Gateway require a valid JWT.

| Method | Path | Behavior |
|---|---|---|
| `POST` | `/api/v1/products` | Create a product |
| `GET` | `/api/v1/products` | List active products; optional `category` filter |
| `GET` | `/api/v1/products/{id}` | Retrieve a product |
| `PUT` | `/api/v1/products/{id}` | Update a product |
| `DELETE` | `/api/v1/products/{id}` | Soft-delete a product |

SKU must be unique; price must be greater than zero. OpenAPI UI is available at `/swagger-ui.html` when the service is accessed directly.
## Inventory Service

Inventory Service owns stock counts for product IDs, independently of the Product Catalog database. Create the ecommerce_inventory database, configure DB_URL, DB_USERNAME, and DB_PASSWORD, then run `mvn spring-boot:run -pl inventory-service` (default port 8083). Flyway creates the schema. The Gateway forwards `/api/v1/inventory/**` to it at `INVENTORY_SERVICE_URL` (default `http://localhost:8083`); API requests through the Gateway require a valid JWT.

| Method | Path | Behavior |
|---|---|---|
| `GET` | `/api/v1/inventory/{productId}` | Retrieve on-hand, reserved, and available quantities |
| `POST` | `/api/v1/inventory/{productId}/adjustments` | Add or subtract stock using an adjustment ID |
| `POST` | `/api/v1/inventory/{productId}/reservations` | Reserve available stock using a client-generated reservation ID |
| `DELETE` | `/api/v1/inventory/{productId}/reservations/{reservationId}` | Release an active reservation |
| `POST` | `/api/v1/inventory/{productId}/reservations/{reservationId}/commit` | Commit a reservation and consume stock |

Adjustment IDs and reservation IDs make retries idempotent. Stock rows use database locking so concurrent reservations cannot consume more than the available quantity. Initial stock is added through a positive adjustment. Reservation quantity must be positive; an insufficient-stock attempt returns `409 Conflict`.
## API Overview

The Auth Service currently includes registration, login, refresh-token functionality, logout, and protected-user functionality. The API Gateway currently routes the `/api/v1/auth/**` path to Auth Service.

| Capability | Purpose |
|---|---|
| User registration | Register a user account |
| User login | Authenticate credentials and issue tokens |
| JWT authentication | Authenticate requests to protected endpoints |
| Current user | Retrieve information about the authenticated user |
| Refresh tokens | Support access-token renewal |
| Logout | Provide an endpoint to log out and invalidate a refresh token |
| Gateway routing | Forward authentication API requests to Auth Service |
| Gateway JWT validation | Verify bearer-token signature and expiration and establish user identity and role authorities |

Refer to the controller implementations for the exact HTTP methods, endpoint paths, request bodies, and response formats.

## Automated Test Results

### Auth Service

**Latest recorded Auth Service verification: 14 tests passed, 0 failures, 0 errors.**

| Test suite | Tests | Result |
|---|---:|---|
| `JwtServiceTest` | 10 | Passed |
| `AuthControllerIntegrationTest` | 4 | Passed |
| **Total** | **14** | **Passed** |

The integration tests use Spring Boot Test and MockMvc with a PostgreSQL container managed by Testcontainers. They cover successful registration, duplicate-email rejection, successful login with token responses, and invalid-password handling.

The executable Spring Boot JAR was successfully packaged during that verification run:

```text
auth-service/target/auth-service-1.0.0-SNAPSHOT.jar
```

### API Gateway

**Latest recorded Gateway test run: 4 tests passed, 0 failures, 0 errors.**

| Test scenario | Expected behavior | Result |
|---|---|---|
| Valid JWT | Authentication succeeds and user identity/authority are created | Passed |
| Expired JWT | Authentication is rejected | Passed |
| Invalid JWT | Authentication is rejected | Passed |
| Tampered JWT payload | Signature verification rejects the modified token | Passed |
| **Total** | | **4 passed** |

The Gateway tests use JUnit 5, Reactor Test, and JJWT with an isolated test signing key. They do not require PostgreSQL or running application services.

### Manual Gateway Verification

The following scenarios have also been manually verified through Postman using port 8090:

| Scenario | Observed result |
|---|---|
| Gateway health endpoint | `UP` |
| User registration through Gateway | Successful |
| User login through Gateway | Successful |
| Protected current-user request with valid access token | `200 OK` |
| Protected current-user request without a token | `401 Unauthorized` |
| Protected current-user request with an invalid token | `401 Unauthorized` |

The manual checks and module-specific test runs are recorded separately; a new full-reactor `mvn clean verify` result should be recorded after it is run.

Refresh-token rotation, old-token reuse rejection, logout revocation, and post-logout refresh rejection still require dedicated integration test coverage.

## Security Practices

Security is a core consideration in this project:

- Use JWTs to authenticate protected API requests.
- Keep signing secrets outside source control.
- Configure the same signing secret in Auth Service and API Gateway while using the current HS256 design.
- Store passwords using an appropriate one-way password hashing algorithm.
- Apply expiration policies to access and refresh tokens.
- Validate refresh tokens before issuing replacement access tokens.
- Use HTTPS in deployed environments.
- Avoid logging passwords, tokens, and other sensitive information.
- Apply authorization rules to protected endpoints and service operations.
- Use database migrations to manage schema changes consistently.
- Keep the Gateway's reactive security implementation separate from the Auth Service's Servlet security filter implementation.

The current HS256 design requires the Gateway to possess the shared signing secret. An asymmetric signing design (Auth Service signs with a private key; Gateway verifies with a public key) can be evaluated as a later hardening step.

Additional security hardening, automated security tests, and production deployment controls will be developed as the project evolves.

## Development Roadmap

### Phase 1 — Foundation and Authentication

- [x] Establish the Maven project structure.
- [x] Create the Auth Service.
- [x] Implement user registration.
- [x] Implement JWT login.
- [x] Configure Spring Security.
- [x] Add JWT authentication filtering.
- [x] Implement refresh-token persistence.
- [x] Add Flyway database migrations.
- [x] Implement a protected current-user endpoint.
- [x] Implement the logout endpoint.
- [x] Add global exception handling.
- [x] Add JWT unit tests.
- [x] Add PostgreSQL-backed integration tests using Testcontainers.
- [x] Verify the Auth Service with Maven (`clean verify`).

### Phase 2 — API Gateway and Authentication Hardening

- [x] Add the API Gateway Maven module.
- [x] Configure Spring Cloud Gateway WebFlux.
- [x] Route `/api/v1/auth/**` requests to Auth Service.
- [x] Configure reactive JWT authentication at the Gateway.
- [x] Validate JWT signature, expiration, subject, and role claims.
- [x] Add unit tests for valid, expired, invalid, and tampered JWTs.
- [x] Manually verify registration and login through the Gateway.
- [x] Manually verify valid-token access and 401 responses for missing and invalid tokens.
- [ ] Add dedicated integration tests for refresh-token rotation and reuse/revocation scenarios.
- [ ] Add dedicated integration tests for logout and refresh-token invalidation.
- [ ] Test refresh and logout flows end to end through the Gateway.
- [ ] Review authentication and authorization edge cases.
- [ ] Evaluate asymmetric JWT signing and verification.
- [ ] Integrate Redis for refresh-token storage and revocation, after validating the storage design.
- [ ] Implement and test TOTP-based multi-factor authentication (MFA).
- [ ] Add API documentation with OpenAPI.

### Phase 3 — E-Commerce Microservices

- [ ] User and Member Service
- [x] Product Catalog Service
- [ ] Shopping Cart Service
- [ ] Order Service
- [ ] Payment Service
- [x] Inventory Service
- [ ] Notification Service

### Phase 4 — Distributed Architecture

- [ ] Integrate Apache Kafka.
- [ ] Introduce Redis caching where appropriate.
- [ ] Implement resilient inter-service communication.
- [ ] Add distributed tracing and centralized observability.

### Phase 5 — Cloud and DevOps

- [ ] Containerize services using Docker.
- [ ] Enhance CI/CD pipelines to run unit and integration tests.
- [ ] Evaluate local Kubernetes deployment before any cloud deployment.
- [ ] Add monitoring, metrics, and alerting.
- [ ] Implement deployment and rollback strategies.
- [ ] Evaluate AWS deployment only if it can be done within an explicitly approved budget.

### Phase 6 — Frontend

- [ ] Develop the Angular frontend.
- [ ] Integrate frontend authentication.
- [ ] Implement shopping, checkout, and order-tracking workflows.

## Engineering Goals

The project emphasizes:

- Clean code and maintainable architecture.
- Secure authentication and authorization.
- Centralized dependency governance.
- Reliable database schema evolution.
- Scalable microservice boundaries.
- Resilient distributed communication.
- Automated testing and CI/CD.
- Observability and operational readiness.
- Cloud-native deployment practices with cost controls.

## Contributing

This repository is actively evolving as new features and architectural improvements are introduced.

Changes should be developed incrementally, tested, and committed using descriptive commit messages.

Example:

```bash
git commit -m "test(gateway): add JWT validation tests"
```

## License

A license has not yet been selected. Until a license is added, reuse and redistribution are subject to applicable copyright law.
