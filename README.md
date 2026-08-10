# E-Commerce Microservices

A hands-on microservices system built with **Java 21 + Spring Boot**, demonstrating
the core patterns of distributed systems: service-to-service communication,
resilience, event-driven sagas, idempotency, observability, and security.

> Learning/portfolio project — built incrementally, one production-minded capability at a time.

## Services

| Service | Port | Responsibility | Status |
|---------|------|----------------|--------|
| `orders-service` | 8081 | Order lifecycle (create, track, refund) | 🚧 In progress |
| `users-service` | 8082 | Customer accounts | ⏳ Planned |
| `payments-service` | 8083 | Payment authorization & refunds | ⏳ Planned |
| `inventory-service` | 8084 | Stock reservation & release | ⏳ Planned |
| `notifications-service` | 8085 | Customer notifications | ⏳ Planned |
| `api-gateway` | 8080 | Single entry point, routing, auth | ⏳ Planned |

## Tech stack

- **Java 21**, **Spring Boot**
- **MySQL** — database-per-service
- **Flyway** — schema migrations
- **Kafka** — async messaging & sagas *(planned)*
- **Resilience4j** — circuit breaker, retry, timeout *(planned)*
- **Keycloak** — OAuth2/JWT identity *(planned)*
- **Micrometer + Zipkin** — distributed tracing *(planned)*

## Architecture principles

- **Database-per-service** — no service reads another's database
- **DTO ≠ Entity** — API and persistence models evolve independently
- **Externalized config** — secrets via environment variables, never committed
- **Contract-first & versioned events** — services stay decoupled
- **SOLID + clean layering** — controller → service → repository per feature

## Running a service locally

Each service is a standalone Spring Boot app. From a service directory:

```bash
# set your DB password once (Windows)
setx DB_PASSWORD "your_mysql_password"

# build & test
./mvnw verify

# run
./mvnw spring-boot:run
```

## Repository layout

```
ecommerce/
├── orders-service/     # each service is an independent Spring Boot project
├── users-service/
└── ...
```
