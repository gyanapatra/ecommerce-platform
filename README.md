# E-Commerce Platform

A scalable, secure, and cloud-ready e-commerce platform built using Java 21, Spring Boot, and a microservices architecture.

The project demonstrates production-oriented backend engineering practices, including centralized dependency management, JWT-based authentication, refresh-token management, database migrations, API security, automated testing, and cloud-native deployment.

> **Project status:** Active development. The foundational Maven structure and core Auth Service functionality are implemented. The Auth Service integration tests run against PostgreSQL using Testcontainers, and the full Maven verification currently passes with 14 tests.

## Key Features

- **JWT authentication:** Token-based authentication for securing APIs.
- **User registration and login:** Authentication endpoints for user access.
- **Refresh-token management:** Persistent refresh tokens with expiration handling.
- **Logout endpoint:** Logout functionality is implemented; dedicated integration coverage for revocation is still planned.
- **Protected APIs:** Spring Security integration with JWT authentication filters.
- **Global exception handling:** Authentication and request errors are mapped to HTTP responses.
- **Database versioning:** Flyway migrations manage database schema changes.
- **Centralized dependency management:** Maven parent project and internal BOM support consistent dependency versions.
- **Shared components:** A common module for reusable application components.
- **Automated testing:** JWT unit tests and PostgreSQL-backed Auth Service integration tests.
- **Cloud-ready architecture:** Designed to support containerization and deployment to AWS.

## Technology Stack

| Category | Technologies |
|---|---|
| Language | Java 21 |
| Backend | Spring Boot 3.5.6, Spring Security |
| Architecture | Microservices |
| Authentication | JWT access tokens, refresh tokens |
| Database | PostgreSQL |
| Persistence | Spring Data JPA, Hibernate |
| Database migrations | Flyway |
| Build | Maven |
| Testing | JUnit, Spring Boot Test, MockMvc, Testcontainers 2.0.5 |
| API security | Spring Security, JWT authentication filters |
| Planned messaging | Apache Kafka |
| Planned caching | Redis |
| Planned deployment | Docker, Kubernetes, AWS EKS |
| Planned frontend | Angular |

Technologies marked as planned will be integrated as development progresses.

## Project Structure

```text
ecommerce-platform/
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

**Auth Service**

Responsible for user registration, authentication, JWT issuance, refresh-token management, logout, and protected API access.

**Common Module**

Provides reusable components shared across application modules.

**E-Commerce BOM**

Centralizes dependency version management to improve consistency and simplify dependency upgrades.

## Authentication Flow

The current authentication design uses access tokens and refresh tokens.

```text
Client
  |
  | 1. Submit login credentials
  v
Auth Service
  |
  | 2. Validate user credentials
  | 3. Generate access token
  | 4. Issue refresh token
  v
Client
  |
  | 5. Call protected API with access token
  v
JWT Authentication Filter
  |
  | 6. Validate token and establish authentication
  v
Protected API
```

When the access token expires, the client can use the refresh token to request a new access token through the Auth Service refresh flow.

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

Typical settings include:

```text
DB_URL=jdbc:postgresql://localhost:5432/ecommerce_auth
DB_USERNAME=ecommerce_user
DB_PASSWORD=your-local-database-password
JWT_SECRET=your-base64-encoded-secret
```

Use the exact property names and formats expected by the application configuration.

**Security:** Never commit real database passwords, JWT signing secrets, access tokens, or refresh tokens to Git.

### 4. Build and Test the Project

From the repository root:

```bash
mvn clean verify
```

The Auth Service's current verification run includes unit tests and PostgreSQL-backed integration tests. Testcontainers requires a working Docker environment and may download the configured PostgreSQL image on the first run.

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

Run the application from your IDE or use Maven from the Auth Service module directory:

```bash
mvn spring-boot:run
```

Ensure PostgreSQL is running and the required environment variables are configured before starting the application. The application port is determined by the current Spring Boot configuration (currently 8081 by default).

## API Overview

The Auth Service currently includes registration, login, refresh-token functionality, logout, and protected-user functionality.

| Capability | Purpose |
|---|---|
| User registration | Register a user account |
| User login | Authenticate credentials and issue tokens |
| JWT authentication | Authenticate requests to protected endpoints |
| Current user | Retrieve information about the authenticated user |
| Refresh tokens | Support access-token renewal |
| Logout | Provide an endpoint to log out and invalidate a refresh token |

Refer to the controller implementations for the exact HTTP methods, endpoint paths, request bodies, and response formats.

## Automated Test Results

**Latest verified Auth Service test run: 14 tests passed, 0 failures, 0 errors.**

| Test suite | Tests | Result |
|---|---:|---|
| `JwtServiceTest` | 10 | Passed |
| `AuthControllerIntegrationTest` | 4 | Passed |
| **Total** | **14** | **Passed** |

The integration tests use Spring Boot Test and MockMvc with a PostgreSQL container managed by Testcontainers. They cover successful registration, duplicate-email rejection, successful login with token responses, and invalid-password handling.

The executable Spring Boot JAR was successfully packaged during the verification run:

```text
auth-service/target/auth-service-1.0.0-SNAPSHOT.jar
```

Refresh-token rotation, logout revocation, and MFA require additional dedicated automated test coverage.

## Security Practices

Security is a core consideration in this project:

- Use JWTs to authenticate protected API requests.
- Keep signing secrets outside source control.
- Store passwords using an appropriate one-way password hashing algorithm.
- Apply expiration policies to access and refresh tokens.
- Validate refresh tokens before issuing replacement access tokens.
- Use HTTPS in deployed environments.
- Avoid logging passwords, tokens, and other sensitive information.
- Apply authorization rules to protected endpoints.
- Use database migrations to manage schema changes consistently.

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

### Phase 2 — Authentication Hardening

- [ ] Add dedicated integration tests for refresh-token rotation and reuse/revocation scenarios.
- [ ] Add dedicated integration tests for logout and refresh-token invalidation.
- [ ] Integrate Redis for refresh-token storage and revocation, after validating the storage design.
- [ ] Implement and test TOTP-based multi-factor authentication (MFA).
- [ ] Add API documentation with OpenAPI.
- [ ] Review authentication and authorization edge cases.

### Phase 3 — E-Commerce Microservices

- [ ] User and Member Service
- [ ] Product Catalog Service
- [ ] Shopping Cart Service
- [ ] Order Service
- [ ] Payment Service
- [ ] Inventory Service
- [ ] Notification Service

### Phase 4 — Distributed Architecture

- [ ] Integrate Apache Kafka.
- [ ] Introduce Redis caching where appropriate.
- [ ] Add an API Gateway.
- [ ] Implement resilient inter-service communication.
- [ ] Add distributed tracing and centralized observability.

### Phase 5 — Cloud and DevOps

- [ ] Containerize services using Docker.
- [ ] Enhance CI/CD pipelines to run unit and integration tests.
- [ ] Deploy services to AWS EKS.
- [ ] Add monitoring, metrics, and alerting.
- [ ] Implement deployment and rollback strategies.

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
- Cloud-native deployment practices.

## Contributing

This repository is actively evolving as new features and architectural improvements are introduced.

Changes should be developed incrementally, tested, and committed using descriptive commit messages.

Example:

```bash
git commit -m "test(auth): add PostgreSQL integration tests"
```

## License

A license has not yet been selected. Until a license is added, reuse and redistribution are subject to applicable copyright law.
