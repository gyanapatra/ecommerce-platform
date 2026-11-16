# E-Commerce Platform

A scalable, secure, and cloud-ready e-commerce platform built using Java 21, Spring Boot, Spring Cloud Gateway, and a microservices architecture.

The project demonstrates production-oriented backend engineering practices, including centralized dependency management, JWT-based authentication, refresh-token management, database migrations, API gateway security, automated testing, and cloud-native development.

> **Project status:** Active development. The Maven multi-module foundation, Auth Service, Member Service, API Gateway, Product Catalog Service, Inventory Service, Shopping Cart Service, Order Service, Payment Service, and Notification Service are implemented. Auth Service registration, login, refresh-token functionality, logout, and protected-user functionality are available. The API Gateway routes requests to all application services and validates JWTs reactively. Four Gateway JWT unit tests have passed. Registration and login through the Gateway, plus valid/unauthorized protected-request scenarios, have also been manually verified.

## Key Features

- **JWT authentication:** Token-based authentication for securing APIs.
- **User accounts and membership:** Auth Service owns credentials and tokens; Member Service owns each signed-in user profile, member number, and membership tier.
- **Refresh-token management:** Persistent refresh tokens with expiration handling.
- **Logout endpoint:** Logout functionality is implemented; dedicated integration coverage for revocation is still planned.
- **API Gateway:** Spring Cloud Gateway Server WebFlux routes authentication, member, product, inventory, cart, order, payment, and notification requests to their services.
- **Inventory Service:** Maintains per-product on-hand stock, reserves quantities, and supports idempotent adjustments, commits, and releases.
- **Shopping Cart Service:** Stores product quantities for each authenticated customer and validates JWTs independently of the Gateway.
- **Order Service:** Checks out the authenticated customer's cart, snapshots catalog prices, coordinates inventory reservations, and records lifecycle events in a transactional outbox. Paid orders cannot be cancelled.
- **Payment Service:** Uses a provider-neutral payment adapter with an idempotent local mock provider; the order total always comes from Order Service.
- **Reactive JWT validation:** Gateway validates JWT signatures and expiration and establishes authenticated user identity and role authorities.
- **Protected APIs:** Spring Security integration with JWT authentication filters in Auth Service and reactive authentication at the Gateway.
- **Notification Service:** Consumes placed, paid, and cancelled order events and stores customer notifications idempotently; authenticated users can list and mark notifications as read.
- **Redis product caching:** Product detail and list reads use Redis with short TTLs; product writes invalidate both caches.
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
| Messaging | Apache Kafka (order lifecycle events) |
| Caching | Redis (Product Catalog read cache) |
| Planned deployment | Docker, Kubernetes |
| Frontend | Angular 22 |

Kafka messaging and Redis caching are now used by the local backend. Cloud deployment remains optional and is not required for local development.

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
├── cart-service/
│   ├── src/main/java/com/product/ecommerce/cart/
│   ├── src/main/resources/db/migration/
│   └── pom.xml
├── inventory-service/
│   ├── src/main/java/com/product/ecommerce/inventory/
│   ├── src/main/resources/db/migration/
│   └── pom.xml
├── order-service/
│   ├── src/main/java/com/product/ecommerce/order/
│   ├── src/main/resources/db/migration/
│   └── pom.xml
├── member-service/
│   ├── src/main/java/com/product/ecommerce/member/
│   ├── src/main/resources/db/migration/
│   └── pom.xml
├── payment-service/
│   ├── src/main/java/com/product/ecommerce/payment/
│   ├── src/main/resources/db/migration/
│   └── pom.xml
├── product-service/
│   ├── src/main/java/com/product/ecommerce/product/
│   ├── src/main/resources/db/migration/
│   └── pom.xml
├── notification-service/
│   ├── src/main/java/com/product/ecommerce/notification/
│   ├── src/main/resources/db/migration/
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

Provides the external entry point for routed API requests. It routes Auth, Member, Product, Inventory, Cart, Order, Payment, and Notification APIs to their services and validates bearer JWTs using Spring Security WebFlux before forwarding protected requests.

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
| PostgreSQL | 5432 | Separate persistence databases for each service |
| Auth Service | 8081 | Registration, sign-in, tokens, and protected user endpoint |
| Member Service | 8087 | Signed-in member profile and membership |
| API Gateway instance 1 | 8090 | Gateway service |
| API Gateway instance 2 | 8091 | Gateway service |
| Front-door load balancer | 8092 | Client-facing NGINX entry point |
| Product Catalog Service | 8082 | Product catalog APIs |
| Inventory Service | 8083 | Stock and reservation APIs |
| Shopping Cart Service | 8084 | Authenticated customer carts |
| Order Service | 8085 | Checkout, order history, inventory reservations, payment confirmation, and outbox publication |
| Payment Service | 8088 | Idempotent local mock payment processing |
| Notification Service | 8086 | Authenticated order notifications |
| Redis | 6379 | Product Catalog read cache |
| Kafka | 9092 | Local order event broker |

The API Gateway health endpoint is available at:

```text
http://localhost:8090/actuator/health
```

The Gateway routes `/api/v1/auth/**`, `/api/v1/members/**`, `/api/v1/products/**`, `/api/v1/inventory/**`, `/api/v1/cart/**`, `/api/v1/orders/**`, `/api/v1/payments/**`, and `/api/v1/notifications/**` to their matching services. Member and payment defaults are `http://localhost:8087` and `http://localhost:8088`. These values can be overridden using `AUTH_SERVICE_URL` and `SERVER_PORT`.

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

Create the database user and one database per service.

Example SQL:

```sql
CREATE USER ecommerce_user WITH PASSWORD 'change-me';
CREATE DATABASE ecommerce_auth OWNER ecommerce_user;
CREATE DATABASE ecommerce_member OWNER ecommerce_user;
CREATE DATABASE ecommerce_payment OWNER ecommerce_user;
```

Replace the example password with a secure local password.

### 3. Configure Environment Variables

Configure `JWT_SECRET` consistently for Auth Service, the Gateway, Member Service, Payment Service, and every other JWT-validating service. Configure database credentials for each service.

Typical Auth Service settings include:

```text
DB_URL=jdbc:postgresql://localhost:5432/ecommerce_auth
DB_USERNAME=ecommerce_user
DB_PASSWORD=your-local-database-password
JWT_SECRET=your-base64-encoded-secret
```

The API Gateway and all resource services also require that same `JWT_SECRET` value. Configure the payment confirmation key with one shared, random value in both Order Service and Payment Service: `PAYMENT_SERVICE_INTERNAL_KEY`. Payment confirmation fails closed when the key is missing or does not match. Keep both secrets out of source control. The Gateway can additionally use:

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

### 7. Start Member and Payment Services

Create `ecommerce_member` and `ecommerce_payment` databases as shown above. Start `member-service` on port 8087 and `payment-service` on port 8088 with the same `JWT_SECRET` as the Gateway and Auth Service. Start `order-service` with the matching `PAYMENT_SERVICE_INTERNAL_KEY`; Payment Service must receive that same key. For local development, generate a key once and copy the same value into both run configurations. Set `MEMBER_SERVICE_URL` and `PAYMENT_SERVICE_URL` on the Gateway only when using non-default addresses.

Checkout places and reserves the order, then the storefront calls Payment Service with the order UUID as the idempotency key. The local `MOCK` provider returns a stable reference and never sends card details. Payment Service confirms the order through the protected internal Order Service endpoint; Order Service emits an `ORDER_PAID` outbox event so Notification Service can deliver the payment update. If payment is pending, the order remains `PLACED` and can be retried from order history.

## Local Kafka and Redis

No cloud account or paid cloud service is needed for this local setup. Start the broker and cache from the repository root with Docker Desktop running:

~~~powershell
docker compose up -d
docker compose ps
~~~

The compose file starts one local Kafka broker and Redis with local data volumes. Kafka is configured for local plaintext development and is not a production security configuration. Stop the containers with docker compose down; this keeps the volumes. Use docker compose down -v only when you also want to erase local Kafka and Redis data.

Product Service uses Redis by default. Order Service stores events in PostgreSQL first and publishes them when Kafka is available; if Kafka is offline, the events remain pending in its outbox and are retried after the broker returns. Set KAFKA_ENABLED=false only when intentionally running without the Kafka publisher or notification consumer.

## Product Catalog Service

The catalog service owns product data and its Flyway-managed PostgreSQL schema. Product detail and active-list reads use Redis (5-minute and 1-minute TTLs); create, update, and soft-delete operations invalidate both caches. Create the `ecommerce_product` database, configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, then start it with `mvn spring-boot:run -pl product-service` (default port `8082`). The Gateway forwards `/api/v1/products/**` to it; configure `PRODUCT_SERVICE_URL` to override the local URL. Catalog requests through the Gateway require a valid JWT.

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

## Shopping Cart Service

Shopping Cart Service stores product UUIDs and quantities in a cart owned by the authenticated customer. It verifies the HS256 access token in addition to Gateway validation, and derives the user ID from the token subject. Create the ecommerce_cart database, configure DB_URL, DB_USERNAME, DB_PASSWORD, and the same JWT_SECRET used by Auth Service, then run `mvn spring-boot:run -pl cart-service` (default port 8084). Flyway creates the schema. The Gateway forwards `/api/v1/cart/**` to CART_SERVICE_URL (default http://localhost:8084).

| Method | Path | Behavior |
|---|---|---|
| `GET` | `/api/v1/cart` | Retrieve the current customer cart |
| `POST` | `/api/v1/cart/items` | Add a product or increase its quantity |
| `PUT` | `/api/v1/cart/items/{productId}` | Set an existing item quantity |
| `DELETE` | `/api/v1/cart/items/{productId}` | Remove an item |
| `DELETE` | `/api/v1/cart/items` | Clear the cart |

The service stores product IDs and quantities. At checkout, Order Service validates active products, snapshots their prices, and requests stock reservations from Inventory Service.

## Order Service

Order Service creates orders from the authenticated customer cart. It loads each product from Product Catalog, snapshots its current name and price, reserves the requested quantity in Inventory, and clears the cart after all reservations succeed. Checkout requires a UUID `Idempotency-Key` header so retries do not create duplicate orders. If one reservation fails, the service releases reservations already created. If a downstream service is temporarily unavailable, retry checkout with the same key to resume the saved processing order.

Create the `ecommerce_order` PostgreSQL database, configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and the same `JWT_SECRET` used by Auth Service, then run `mvn spring-boot:run -pl order-service` (default port `8085`). Cart, Product, and Inventory URLs can be overridden with `CART_SERVICE_URL`, `PRODUCT_SERVICE_URL`, and `INVENTORY_SERVICE_URL`. The Gateway forwards `/api/v1/orders/**` to `ORDER_SERVICE_URL` (default `http://localhost:8085`).

| Method | Path | Behavior |
|---|---|---|
| `POST` | `/api/v1/orders` | Create an order from the current cart; requires `Idempotency-Key` |
| `GET` | `/api/v1/orders` | List orders for the authenticated user |
| `GET` | `/api/v1/orders/{orderId}` | Retrieve an owned order |
| `POST` | `/api/v1/orders/{orderId}/payment-confirmation` | Internal Payment Service confirmation; requires the service key |
| `POST` | `/api/v1/orders/{orderId}/cancel` | Cancel an unpaid placed order and release its inventory reservations |

Orders snapshot product names, quantities, unit prices, and totals.
Every order state change is written to the PostgreSQL outbox in the same database transaction. A background publisher retries unpublished records and sends them to Kafka; delivery is at-least-once, so consumers de-duplicate by event ID. Payment confirmation is integrated: successful payments mark orders paid, publish an outbox event, and notify customers. Unpaid placed orders keep stock reserved and can be cancelled.

## Member Service

Member Service owns customer profile details and membership metadata, keyed by the authenticated Auth Service user UUID. It uses its own `ecommerce_member` PostgreSQL database and Flyway migration. A profile is created or updated with `PUT`; member number, tier, and status are generated and managed by the service. Requests require a valid JWT through the Gateway.

| Method | Path | Behavior |
|---|---|---|
| `GET` | `/api/v1/members/me` | Retrieve the signed-in member profile (`404` before profile creation) |
| `PUT` | `/api/v1/members/me` | Create or update first name, last name, and optional phone number |

The storefront’s Member screen lets the signed-in customer create or edit their profile. The service does not store credentials; Auth Service remains the identity and token owner.

## Payment Service

Payment Service owns payment attempts in its own `ecommerce_payment` PostgreSQL database. It currently registers a provider-neutral `PaymentProvider` interface with a deterministic local `MOCK` implementation. It does not accept client-supplied amounts or card details. It loads the owned order and authoritative total from Order Service, permits payment only while the order is `PLACED`, and enforces one payment per order plus idempotent retries.

| Method | Path | Behavior |
|---|---|---|
| `POST` | `/api/v1/payments` | Create or retry payment; requires `Idempotency-Key` and `{ "orderId": "…", "paymentMethod": "MOCK" }` |
| `GET` | `/api/v1/payments/{paymentId}` | Retrieve an owned payment |

Both Order Service and Payment Service must receive the same private `PAYMENT_SERVICE_INTERNAL_KEY`. Payment Service supplies it when confirming a payment; Order Service fails closed if it is absent or incorrect. The customer token is also forwarded, and Order Service checks that the customer owns the order. Successful confirmation marks the order `PAID`; the outbox publishes `ORDER_PAID` for Notification Service. This mock provider is for local development only and does not contact a payment network.
## Notification Service

Notification Service consumes ORDER_PLACED, ORDER_PAID, and ORDER_CANCELLED records from ecommerce.order-events.v1 and stores one in-app notification per event ID. Kafka retries a failed message three times and then routes it to ecommerce.order-events.v1.DLT for inspection. The consumer persists before its offset is committed, and the unique event ID makes redelivery safe.

Create the ecommerce_notification PostgreSQL database, configure DB_URL, DB_USERNAME, DB_PASSWORD, and the same JWT_SECRET as Auth Service, start Kafka, then run mvn spring-boot:run -pl notification-service (default port 8086). In Postman, run Order Management first to create a notification, then run the Notification Service folder. The Gateway forwards /api/v1/notifications/** to NOTIFICATION_SERVICE_URL (default http://localhost:8086).

| Method | Path | Behavior |
|---|---|---|
| GET | /api/v1/notifications | List notifications for the authenticated user |
| PUT | /api/v1/notifications/{notificationId}/read | Mark an owned notification as read |

Notifications are persisted in this service's database. No email provider or cloud service is called.

## API Overview

The Auth Service currently includes registration, login, refresh-token functionality, logout, and protected-user functionality. Notification Service adds authenticated in-app order notifications. The API Gateway routes Auth, Member, Product, Inventory, Cart, Order, Payment, and Notification API requests to their corresponding services.

| Capability | Purpose |
|---|---|
| User registration | Register a user account |
| User login | Authenticate credentials and issue tokens |
| JWT authentication | Authenticate requests to protected endpoints |
| Current user | Retrieve information about the authenticated user |
| Refresh tokens | Support access-token renewal |
| Logout | Provide an endpoint to log out and invalidate a refresh token |
| Gateway routing | Forward Auth, Product, Inventory, Cart, Order, and Notification API requests to their services |
| Order events | Transactional outbox delivers placed and cancelled lifecycle events through Kafka |
| Notifications | Store and retrieve de-duplicated order notifications per authenticated user |
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

### Product Catalog Service

Latest Product Catalog verification: 3 tests passed, including a PostgreSQL-and-Redis Testcontainers integration test for cached reads, JSON serialization of timestamp fields, and cache invalidation after product changes.

### Notification Service

Latest Notification Service verification: 1 test passed, confirming duplicate Kafka event delivery creates only one notification.

### Order Service

Latest Order Service verification: 10 tests passed, 0 failures, 0 errors, including PostgreSQL Testcontainers migration and persistence coverage. The tests also cover cart checkout, price snapshots, inventory reservations, idempotent retries, reservation compensation, cancellation, and JWT validation.

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

The latest full-reactor verification completed on 2026-11-14: 45 tests passed, with 0 failures and 0 errors, across all 10 Maven modules.

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

- [x] User and Member Service
- [x] Product Catalog Service
- [x] Shopping Cart Service
- [x] Order Service
- [x] Payment Service
- [x] Inventory Service
- [x] Notification Service

### Phase 4 — Distributed Architecture

- [x] Integrate Apache Kafka for order lifecycle events using a transactional outbox and notification consumer.
- [x] Introduce Redis caching for Product Catalog reads with write invalidation.
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

- [x] Develop a professional Angular storefront in `frontend/`, aligned with catalog, authentication, cart, checkout, order history, and notification APIs.
- [x] Integrate frontend authentication and token refresh.
- [x] Implement shopping, checkout, order history, and order notifications. Payment processing remains a backend follow-up.

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





