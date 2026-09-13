# Signeasy

Multi-tenant SaaS subscription API built with Spring Boot 3.3 and Java 21. The project follows a hexagonal architecture that separates business rules, application services and adapters (web/JPA). The application exposes secured endpoints to manage customers, plans and subscriptions, deriving the tenant and roles from JWT tokens.

## Layered architecture
- `domain`: business models (`Customer`, `Plan`, `PlanPrice`, `Subscription`), domain types (`Period`, `PlanType`) and business exceptions, without JPA or Jackson dependencies.
- `application`: transactional services enforcing rules (e.g., unique email checks, trial/next billing calculation) and defining ports (`CustomerRepositoryPort`, `TenantContext`, etc.).
- `adapters/persistence-jpa`: JPA entities and composite keys, explicit domain mappers and transactional implementations of the ports, with Flyway migrations (`db/migration`) and PostgreSQL support.
- `adapters/web`: REST API, logging filters, automatic user provisioning and OAuth2 Resource Server / JWT integration.

```
.
├── domain/                # Domain core
├── application/           # Services and ports
├── adapters/
│   ├── persistence-jpa/   # Database adapter (Spring Data + Flyway)
│   └── web/               # HTTP API + security + documentation
├── docker/                # Dockerfile, docker-compose and Keycloak realm
└── pom.xml                # Maven multi-module build
```

## Key features

Customer audit timestamps (`createdAt`, `updatedAt`) belong exclusively to persistence. JPA callbacks generate them on insert and refresh `updatedAt` on changes; domain models and HTTP DTOs do not expose them. Repository updates preserve existing audit data. Related entities are resolved by composite key without cascading changes to nested domain objects, and returned domain graphs are fully materialized inside the adapter transaction (`open-in-view=false`).

- Tenant-aware customer management with unique email validation.
- Plan tiers (type, name, trial days) decoupled from pricing: each tier can have several `PlanPrice` variants, one per billing period (e.g. `PRO` monthly and `PRO` yearly), each with its own price.
- Subscription lifecycle: create, change plan and cancel with automatic date handling.
- Multi-tenancy based on the `tenantId` claim from JWT, propagated through `TenantContextHolder`.
- Automatic `Customer` provisioning for the authenticated user (claims `sub`, `email`, `name`).
- Observability via Actuator, documentation with Springdoc OpenAPI and structured logging (MDC + request/response).

## Requirements
- Java 21+
- Maven 3.9+
- Docker and Docker Compose (optional, required for Testcontainers and full stack)
- PostgreSQL 16 (local or via Docker)

## Running locally (Maven)
1. Start PostgreSQL with the default credentials from `application.yml`:
   ```bash
   docker run --name subscriptions-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
     -e POSTGRES_DB=subscriptions -p 5432:5432 -d postgres:16
   ```
2. (Optional) Override connection variables if using a different instance (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).
3. Build and start the API:
   ```bash
   mvn -pl adapters/web -am spring-boot:run
   ```
4. Swagger UI lives at `http://localhost:8080/swagger`, health at `http://localhost:8080/actuator/health`.

> For quick tests without Keycloak, set `APP_SECURITY_JWT_PROVIDER=standard` and provide JWTs with the `roles` claim. When using Keycloak keep the default (`keycloak`) and issue tokens from the realm exported at `docker/saas-realm-export.json`.

## Running with Docker Compose
1. Enter the `docker` folder and start the stack:
   ```bash
   cd docker
   docker compose up --build
   ```
2. Services available:
   - API at `http://localhost:8080`
   - PostgreSQL (application) at `localhost:5432`
   - Keycloak at `http://localhost:8082` (admin/admin)
   - PgAdmin at `http://localhost:8081` (admin@example.com / admin)
3. The `saas` realm is imported automatically; issue tokens for the `subscriptions-api` client.

## Configuration
| Property                                         | Default                                          | Description |
|--------------------------------------------------|--------------------------------------------------|-------------|
| `spring.datasource.url`                          | `jdbc:postgresql://localhost:5432/subscriptions` | Primary database URL |
| `spring.datasource.username` / `password`        | `postgres` / `postgres`                          | Database credentials |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `http://localhost:8082/realms/saas`         | OAuth2 provider issuer |
| `app.security.jwt.provider`                      | `keycloak` (`standard` for simple tokens)        | Selects the authority extractor |
| `app.security.jwt.keycloak.client-id`            | `subscriptions-api`                              | Client ID used to read resource-access roles |
| `server.port`                                    | `8080`                                           | HTTP port |

All properties can be overridden through environment variables (`SPRING_DATASOURCE_URL`, `APP_SECURITY_JWT_PROVIDER`, etc.).

## Security & multi-tenancy
- The API acts as an OAuth2 Resource Server, accepting bearer JWT tokens.
- `TenantContextFilter` captures the authenticated JWT, extracts `tenantId` (list or string) and stores it in `TenantContextHolder` for downstream layers.
- `TenantContext`, exposed by `RequestTenantContext`, throws if the tenant is missing.
- `JwtAuthoritiesExtractor` offers two implementations:
  - `KeycloakJwtAuthoritiesExtractor`: merges roles from `realm_access.roles` and `resource_access[clientId].roles`.
  - `StandardJwtAuthoritiesExtractor`: reads a simple `roles` claim.
- `UserProvisioningFilter` ensures the authenticated user has a `Customer` record using `sub`, `email` and `name` claims.

## HTTP API
| Method | Path                                  | Description                                                       | Required roles |
|--------|----------------------------------------|-------------------------------------------------------------------|----------------|
| POST   | `/api/customers`                       | Create a customer for the current tenant                          | `ROLE_ADMIN`   |
| GET    | `/api/customers?email=`                | Retrieve a customer by email within the tenant                    | Any authenticated user |
| POST   | `/api/plans`                           | Register a plan tier (type, name, trial)                          | `ROLE_ADMIN`   |
| GET    | `/api/plans`                           | List active plan tiers for the tenant                              | `ROLE_ADMIN`, `ROLE_TENANT_ADMIN`, `ROLE_USER` |
| POST   | `/api/plans/prices`                    | Register a price for a plan tier (type, period, price)            | `ROLE_ADMIN`   |
| GET    | `/api/plans/prices`                    | List active plan prices for the tenant                             | `ROLE_ADMIN`, `ROLE_TENANT_ADMIN`, `ROLE_USER` |
| POST   | `/api/subscriptions`                   | Create a subscription for a customer/plan price                   | `ROLE_TENANT_ADMIN`, `ROLE_USER` |
| POST   | `/api/subscriptions/{id}/change-plan`  | Switch the plan price of an existing subscription                 | `ROLE_TENANT_ADMIN`, `ROLE_USER` |
| POST   | `/api/subscriptions/{id}/cancel`       | Cancel a subscription and set the end date                        | `ROLE_TENANT_ADMIN`, `ROLE_USER` |

### Payload examples
```json
POST /api/customers
{
  "name": "Alice",
  "email": "alice@example.com"
}
```

```json
POST /api/plans
{
  "planType": "BASIC",
  "name": "Basic Plan",
  "trialDays": 14
}
```

```json
POST /api/plans/prices
{
  "planType": "BASIC",
  "period": "MONTHLY",
  "priceCents": 1990
}
```

A plan tier can have one price per period (e.g. `BASIC`/`MONTHLY` and `BASIC`/`YEARLY` side by side) — `(tenantId, planType, period)` is the uniqueness key, so different periods of the same tier no longer collide.

```json
POST /api/subscriptions
{
  "customerId": "0c54c6b9-0ad5-4c6e-b5f4-3c4aa0d09450",
  "planType": "BASIC",
  "period": "MONTHLY"
}
```

```json
POST /api/subscriptions/{id}/change-plan
{
  "newPlanType": "PRO",
  "newPeriod": "MONTHLY"
}
```

`PlanType` accepts `BASIC` or `PRO`; `Period` accepts `MONTHLY` or `YEARLY`.

## Observability & logging
- `RequestResponseLoggingFilter` logs HTTP method, URL, status and payloads (with simple sanitization).
- `LoggingMdcFilter` adds `correlationId` (`X-Request-Id` header or random UUID), IP and authenticated user to the MDC.
- Logs are sent to the console and `./logs/spring-boot-logger-log4j2.log` with daily rolling.
- Actuator exposes health/info; Swagger UI is served at `/swagger` with OpenAPI at `/v3/api-docs`.

## Tests
- Run unit tests: `mvn test`.
- Full suite (including Testcontainers):
  ```bash
  mvn verify
  ```
  Ensure Docker is running so the PostgreSQL test container can start.

## Suggested next steps
- Provide scripts/examples to generate JWTs for local testing (Keycloak or mocked tokens).
- Expand integration test coverage for the exposed endpoints.
- Add extra monitoring (custom metrics) if needed.

## Subscription identity

A subscription is identified by `(id, tenantId)`. Creation assigns a random UUID before persistence; changing its plan or canceling it preserves that identity. Repository lookups use the subscription ID, not the customer ID.

Subscription responses now contain only `id` and `tenantId` in `key`. The former `key.customerId` and `key.planId` fields have been removed; related identifiers remain available through `customer.key` and `planPrice.key`.

Subscriptions reference a `PlanPrice` (tier + period + price), not a `Plan` tier directly — see [Plan pricing model](#plan-pricing-model) below for why.

Run unit and PostgreSQL/Testcontainers integration tests with Java 21 and Docker available:

```sh
mvn -B verify
```

To run the subscription integration tests alongside the existing unit tests:

```sh
mvn -B verify -Dit.test=SubscriptionPersistenceIT,SubscriptionHttpIT -Dfailsafe.failIfNoSpecifiedTests=false
```

## Plan pricing model

`Plan` used to hold a single `period`/`priceCents` pair, so `(tenantId, planType)` was the plan's uniqueness key — a tenant could not have both a monthly and a yearly `PRO` plan at the same time. `Plan` now models only the tier (type, name, trial days); pricing lives in `PlanPrice`, one row per `(tenantId, planId, period)`. A subscription points at a `PlanPrice`, so it carries both the tier and the billing period it was sold at. Creating two prices for the same tier with different periods is expected and no longer collides.
