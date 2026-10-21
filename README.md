# E-Commerce Platform

A scalable, secure, and cloud-ready e-commerce platform built using Java, Spring Boot, and a microservices architecture.

The project aims to demonstrate production-oriented backend engineering practices, including centralized dependency management, JWT-based authentication, refresh token management, database migrations, API security, distributed messaging, and cloud-native deployment.

> **Project status:** Active development. The initial platform structure and core Auth Service functionality have been implemented. Additional microservices and infrastructure components are planned.

## Key Features

* **JWT Authentication:** Token-based authentication for securing APIs.
* **Refresh Token Management:** Persistent refresh tokens with expiration handling.
* **User Registration and Login:** Authentication endpoints for user access.
* **Protected APIs:** Spring Security integration with JWT authentication filters.
* **Database Versioning:** Flyway migrations for managing database schema changes.
* **Centralized Dependency Management:** Maven-based project structure designed for consistent dependency versions.
* **Shared Components:** Common module for reusable application components.
* **Cloud-Ready Architecture:** Designed to support containerization and deployment to AWS.

## Technology Stack

| Category            | Technologies                                |
| ------------------- | ------------------------------------------- |
| Language            | Java 21                                     |
| Backend             | Spring Boot, Spring Security                |
| Architecture        | Microservices                               |
| Authentication      | JWT, refresh tokens                         |
| Database            | PostgreSQL                                  |
| Persistence         | Spring Data JPA, Hibernate                  |
| Database Migrations | Flyway                                      |
| Build               | Maven                                       |
| API Security        | Spring Security, JWT authentication filters |
| Planned Messaging   | Apache Kafka                                |
| Planned Caching     | Redis                                       |
| Planned Deployment  | Docker, Kubernetes, AWS EKS                 |
| Planned Frontend    | Angular                                     |

*The planned technologies will be integrated as development progresses.*

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
│   └── pom.xml
├── common/
│   └── pom.xml
├── ecommerce-bom/
│   └── pom.xml
├── pom.xml
└── README.md
```

The structure above illustrates the intended project organization. Some directories or modules may be added as development progresses.

### Core Components

**Auth Service**

Responsible for user authentication, JWT issuance, refresh token management, and protected API access.

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

When the access token expires, the client can use the refresh token to request a new access token through the refresh flow implemented by the Auth Service.

## Getting Started

### Prerequisites

Install the following software:

* JDK 21
* Git
* Maven, or use the Maven Wrapper if available
* PostgreSQL
* An IDE such as IntelliJ IDEA, Eclipse, or VS Code

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

Use the exact property names and formats expected by your application configuration.

**Security:** Never commit real database passwords, JWT signing secrets, access tokens, or refresh tokens to Git.

### 4. Build the Project

From the repository root:

```bash
mvn clean verify
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

The build command depends on the modules and tests currently configured in the project.

### 5. Start the Auth Service

Run the application from your IDE or use Maven from the appropriate module directory:

```bash
mvn spring-boot:run
```

Ensure PostgreSQL is running and the required environment variables are configured before starting the application.

The application port is determined by the current Spring Boot configuration.

## API Overview

The Auth Service currently includes registration, login, and protected-user functionality.

| Capability         | Purpose                                           |
| ------------------ | ------------------------------------------------- |
| User Registration  | Register a user account                           |
| User Login         | Authenticate credentials and issue tokens         |
| JWT Authentication | Authenticate requests to protected endpoints      |
| Current User       | Retrieve information about the authenticated user |
| Refresh Tokens     | Support access-token renewal                      |

Refer to the controller implementations for the exact HTTP methods, endpoint paths, request bodies, and response formats.

## Security Practices

Security is a core consideration in this project.

* Use JWTs to authenticate protected API requests.
* Keep signing secrets outside source control.
* Store passwords using an appropriate one-way password hashing algorithm.
* Apply expiration policies to access and refresh tokens.
* Validate refresh tokens before issuing replacement access tokens.
* Use HTTPS in deployed environments.
* Avoid logging passwords, tokens, and other sensitive information.
* Apply authorization rules to protected endpoints.
* Use database migrations to manage schema changes consistently.

Additional security hardening, automated security tests, and production deployment controls will be developed as the project evolves.

## Development Roadmap

### Phase 1 — Foundation and Authentication

* [x] Establish the Maven project structure.
* [x] Create the Auth Service.
* [x] Implement user registration.
* [x] Implement JWT login.
* [x] Configure Spring Security.
* [x] Add JWT authentication filtering.
* [x] Implement refresh token persistence.
* [x] Add Flyway database migrations.
* [x] Implement a protected current-user endpoint.

### Phase 2 — Authentication Hardening

* [ ] Implement and test refresh-token rotation.
* [ ] Implement logout and token revocation.
* [ ] Add automated unit and integration tests.
* [ ] Add API documentation with OpenAPI.
* [ ] Review authentication and authorization edge cases.

### Phase 3 — E-Commerce Microservices

* [ ] User and Member Service
* [ ] Product Catalog Service
* [ ] Shopping Cart Service
* [ ] Order Service
* [ ] Payment Service
* [ ] Inventory Service
* [ ] Notification Service

### Phase 4 — Distributed Architecture

* [ ] Integrate Apache Kafka.
* [ ] Introduce Redis caching where appropriate.
* [ ] Add an API Gateway.
* [ ] Implement resilient inter-service communication.
* [ ] Add distributed tracing and centralized observability.

### Phase 5 — Cloud and DevOps

* [ ] Containerize services using Docker.
* [ ] Implement CI/CD pipelines.
* [ ] Deploy services to AWS EKS.
* [ ] Add monitoring, metrics, and alerting.
* [ ] Implement deployment and rollback strategies.

### Phase 6 — Frontend

* [ ] Develop the Angular frontend.
* [ ] Integrate frontend authentication.
* [ ] Implement shopping, checkout, and order-tracking workflows.

## Engineering Goals

The project emphasizes:

* Clean code and maintainable architecture.
* Secure authentication and authorization.
* Centralized dependency governance.
* Reliable database schema evolution.
* Scalable microservice boundaries.
* Resilient distributed communication.
* Automated testing and CI/CD.
* Observability and operational readiness.
* Cloud-native deployment practices.

## Contributing

This repository is actively evolving as new features and architectural improvements are introduced.

Changes should be developed incrementally, tested, and committed using descriptive commit messages.

Example:

```bash
git commit -m "feat(auth): implement refresh token rotation"
```

## License

A license has not yet been selected. Until a license is added, reuse and redistribution are subject to applicable copyright law.
