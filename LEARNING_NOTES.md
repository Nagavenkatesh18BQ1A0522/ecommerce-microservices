# E-Commerce Microservices — Learning Notes

A plain-English record of what we built, **why**, and the concepts behind each piece.
Use it for revision and interviews.

---

## 1. The big picture

We're building a mini e-commerce system as **independent microservices**. So far:

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| `orders-service` | 8081 | `orders_db` | Create & fetch orders |
| `inventory-service` | 8082 | `inventory_db` | Track stock per product |

Two ways they talk:
- **Synchronous** (Phase 1): `orders` calls `inventory` over HTTP to check stock, *now*, and waits for the answer.
- **Asynchronous** (Phase 2, in progress): `orders` publishes an **event** to Kafka; other services react whenever they can. No waiting.

**The #1 idea:** each service owns its own database and is independently deployable. No service reaches into another's database — they communicate over the network (HTTP or events).

---

## 2. Phase 0 — the `orders-service` foundation

We built one full "vertical slice" — a feature that goes through every layer — for creating an order.

**Layers (package-by-feature, everything for `order` in one package):**
```
HTTP request → Controller → Service → Repository → Database
```

| Component | What it does | Key concept |
|-----------|--------------|-------------|
| `OrderController` | Handles HTTP (`POST /orders`, `GET /orders/{id}`) | `@RestController`; returns JSON; `201 Created` on create, `200` on read |
| `OrderService` | Business logic, orchestration | `@Service`; `@Transactional`; the only place that "decides" things |
| `OrderRepository` | Database access | `extends JpaRepository` — Spring **generates** the implementation |
| `Order` | The database entity | `@Entity`; owns its own valid state |
| `OrderStatus` | Fixed set of statuses | `enum` — type-safe, can't be a typo |
| DTOs (`CreateOrderRequest`, `OrderResponse`) | Data crossing the API boundary | Java **records** — immutable data carriers |
| `V1__create_orders_table.sql` | Creates the DB table | **Flyway** migration |
| `GlobalExceptionHandler` | Turns errors into clean responses | `@RestControllerAdvice` + RFC 7807 `ProblemDetail` |

**Concepts & why they matter:**
- **DTO ≠ Entity** — the API model (`OrderResponse`) is separate from the DB model (`Order`). So you can change the database without breaking the API, and a client can never set a field it shouldn't (like `id` or `status`).
- **Records for DTOs** — immutable, and you get constructor/getters/equals/hashCode/toString for free. Perfect for "just data."
- **`enum` not `String`** for status — the compiler guarantees only valid values. Stored as text in the DB (`EnumType.STRING`) so reordering the enum never corrupts existing rows.
- **Constructor injection (`private final`)** — dependencies are immutable, explicit, and easy to test. No `@Autowired` on fields.
- **`@Transactional` on the service** — the write runs in a DB transaction; if anything throws, it rolls back. The service (not the controller/repository) is the right transaction boundary.
- **Flyway owns the schema** — schema changes are versioned SQL files, applied automatically and identically everywhere. Hibernate is set to `validate` (check only), never `update`. Flyway = version control for your database.
- **Bean Validation** (`@NotNull`, `@NotBlank`, `@Positive`) + `@Valid` — invalid requests are rejected with `400` *before* your code runs.
- **`ProblemDetail`** — a standardized error shape (RFC 7807) with per-field messages, instead of a generic 500/400.

---

## 3. Phase 1 — inter-service communication + resilience

### 3a. The synchronous call
When an order is placed, `orders` calls `inventory` to check stock:
```
POST /orders → OrderService → InventoryClient → GET http://inventory/inventory/{productCode}
             → not enough stock? → 409 Conflict
```
- `InventoryClient` uses Spring's **`RestClient`** (the modern synchronous HTTP client).
- **`InventoryView`** is `orders`' *own* DTO for inventory's response — `orders` does **not** import inventory's classes. Each service owns its model; this keeps them **decoupled**.
- Business rule: `availableQuantity < requested` → throw `InsufficientStock` → `409 Conflict`.
- Config (`inventory.base-url`) is **externalized** in `application.yml`, not hardcoded in Java.

### 3b. Resilience (Resilience4j) — surviving a dependency failure
The danger: if `inventory` is down, a naive HTTP call hangs or 500s, and one dead service takes down order creation. We wrapped the call with four layers:

| Pattern | What it does | Our setting |
|---------|--------------|-------------|
| **Timeout** | Don't wait forever for a slow/dead service | 2s connect + 2s read |
| **Retry** | Try again on a transient blip | 3 attempts, 0.5s apart |
| **Circuit breaker** | If it keeps failing, *stop calling* it and fail fast | open at 50% failure over 10 calls, 10s cooldown |
| **Fallback** | Return a controlled response instead of a crash | throw `InventoryUnavailable` → `503` |

**The lesson:** *a failing dependency must not cascade.* We verified it live — killed inventory, watched orders retry, the circuit **open** (instant `503`s), then **auto-recover** when inventory came back. That's the single most important microservices behavior.

---

## 4. Phase 2 — Kafka (async, in progress)

Same Orders/Inventory idea, done with **events** instead of a direct call.

**Done so far (producer side):**
- `OrderPlaced` — an **event** = a past-tense *fact* ("an order was placed"), carrying the data others need. Not a command. The publisher doesn't know or care who listens.
- `OrderEventPublisher` — uses **`KafkaTemplate`** to publish `OrderPlaced` to a topic (`orders.order-placed`), keyed by order id.
- **Fire-and-forget** — publishing must not block or fail order creation.
- Verified with an **`@EmbeddedKafka`** test — a real Kafka broker running inside the test JVM (pulled via Maven, since we couldn't download the broker binary through the corporate proxy).

**Sync vs async — the comparison (a great interview point):**
- *Synchronous* (Phase 1): immediate answer, simple to reason about, but the caller is **coupled** to the callee being up and fast.
- *Asynchronous* (Phase 2): the caller just emits an event and moves on; services are **decoupled** and resilient to each other's downtime, at the cost of **eventual consistency** (the result isn't instant).

**Coming next:** inventory *consumes* `OrderPlaced` and reserves stock; then the full **choreography saga** (inventory publishes a result, orders updates the order status), plus **idempotency** (don't double-process a redelivered event).

---

## 5. Cross-cutting "senior habits" we followed

- **Package by feature**, not by layer (`order/`, `stock/`) — everything about a feature lives together; enables real encapsulation.
- **One responsibility per class** — controller = HTTP, service = logic, repository = data, publisher = messaging.
- **Externalized config & secrets** — DB password via an environment variable (`DB_PASSWORD`), never in a file or git.
- **Tests as a safety net** — `@WebMvcTest` (controller slices), pure Mockito (service unit tests with `ArgumentCaptor`/`verify`), `@EmbeddedKafka` (real broker in tests). Green tests let you refactor fearlessly.
- **Clean git history** — one focused change per commit, feature branch → Pull Request → review → merge.
- **Meaningful HTTP status codes** — `201` create, `200` read, `400` bad input, `404` not found, `409` conflict, `503` dependency down.

---

## 6. Key decisions & the "why" (interview gold)

- **Spring Boot 4 targeting Java 21 on JDK 25** — modern features, broad compatibility.
- **MySQL, database-per-service** — logical separation (`orders_db`, `inventory_db`) enforces the "no shared database" rule.
- **`RestClient` over `RestTemplate`/Feign** — `RestClient` is the modern synchronous client; Feign is in maintenance.
- **Choreography (events) over orchestration** for the saga — services react to events independently; no central conductor. (Trade-off: harder to see the whole flow in one place.)
- **Embedded Kafka instead of a real broker** — forced by the no-download environment, but a legitimate, standard testing approach.
- **We build patterns when we feel the need** — no gateway/Eureka/config-server yet; we add each only when a real problem calls for it. Microservices ≠ a fixed checklist of tools.

---

## 7. Environment / Spring Boot 4 gotchas we hit (real-world debugging)

These are the kind of issues that separate "followed a tutorial" from "actually built it":

- **Boot 4 modularized its auto-configuration.** Several integrations need a specific module the base starter doesn't pull:
  - `RestClient.Builder` isn't auto-configured on the webmvc starter → built the client with the static `RestClient.builder()`.
  - Resilience4j: must use `resilience4j-spring-boot4` (the `-spring-boot3` module hard-fails on Boot 4).
  - Kafka: must add `spring-boot-starter-kafka` (Boot 4 split Kafka auto-config out; bare `spring-kafka` gives no `KafkaTemplate` bean).
- **Boot 4 renamed/moved packages** — `@WebMvcTest` moved to `org.springframework.boot.webmvc.test.autoconfigure`; `@MockBean` → `@MockitoBean`.
- **A stray `spring-aop` test-scoped dependency** in the pom overrode the correct version and broke the runtime classpath — lesson: don't manually declare framework libs that arrive transitively; watch version *and* scope.
- **"Tests green" ≠ "app runs"** — a test-scoped dependency made tests pass while the app failed to start. Always run the app too.
- **Corporate proxy blocks large downloads** — Maven works (small artifacts), but big binaries (Kafka) don't. Solution: get everything through Maven (embedded Kafka).

---

## 8. Git workflow — the rule that kept biting us

**Always `pull main` FIRST — before deleting a branch and before creating a new one.**

The safe cycle:
```
finish feature → open PR → review → merge → checkout main → PULL → delete old branch
   → (next task) branch from the freshly-pulled main → work → repeat
```
Skipping the pull is what caused "my code disappeared" every time (you branched off a stale `main`). A branch is just a movable label — the commits are safe; you just weren't pointing at them.

---

## 9. Where we are & what's next

**Done:** `orders-service` (create + fetch, validated, tested), `inventory-service` (stock lookup), synchronous inter-service call with full resilience, Kafka producer (`OrderPlaced`) — all merged to `main`.

**Next (Phase 2 continued):**
1. `inventory-service` consumes `OrderPlaced` (`@KafkaListener`) and reserves stock.
2. Full choreography saga: inventory publishes `StockReserved`/`StockRejected` → orders sets the order `CONFIRMED`/`CANCELLED`.
3. Idempotency (a `processed_events` table) so redelivered events don't double-process.

**Later phases:** API gateway, security (Keycloak/JWT), observability (tracing), the transactional outbox pattern, and Kubernetes.
