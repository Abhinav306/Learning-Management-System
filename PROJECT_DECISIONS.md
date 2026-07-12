# 📐 PROJECT DECISIONS — Architecture Decision Records

> **Project:** AI-Powered Learning Management System  
> **Purpose:** This document captures all significant architectural and technical decisions made during the design and development of the LMS. Each decision is recorded as an **Architecture Decision Record (ADR)** to provide context, rationale, alternatives considered, and consequences for current and future team members.  
> **Maintained by:** Backend Architecture Team  
> **Last Updated:** 2026-07-11

---

## Table of Contents

| ADR | Title | Status |
|-----|-------|--------|
| [ADR-001](#adr-001-monolith-first-architecture) | Monolith-First Architecture | ✅ Accepted |
| [ADR-002](#adr-002-feature-based-package-structure) | Feature-Based Package Structure | ✅ Accepted |
| [ADR-003](#adr-003-uuid-primary-keys) | UUID Primary Keys | ✅ Accepted |
| [ADR-004](#adr-004-baseentity-with-jpa-auditing) | BaseEntity with JPA Auditing | ✅ Accepted |
| [ADR-005](#adr-005-service-interface--implementation-pattern) | Service Interface + Implementation Pattern | ✅ Accepted |
| [ADR-006](#adr-006-mapstruct-for-object-mapping) | MapStruct for Object Mapping | ✅ Accepted |
| [ADR-007](#adr-007-separate-requestresponse-dtos) | Separate Request/Response DTOs | ✅ Accepted |
| [ADR-008](#adr-008-generic-apiresponse-wrapper) | Generic ApiResponse Wrapper | ✅ Accepted |
| [ADR-009](#adr-009-global-exception-handling-with-restcontrolleradvice) | Global Exception Handling with @RestControllerAdvice | ✅ Accepted |
| [ADR-010](#adr-010-spring-profiles-dev-test-prod) | Spring Profiles (dev, test, prod) | ✅ Accepted |
| [ADR-011](#adr-011-hibernate-ddl-auto-now-flyway-later) | Hibernate DDL-Auto Now, Flyway Later | ✅ Accepted |
| [ADR-012](#adr-012-jwt-authentication-access--refresh-tokens) | JWT Authentication (Access + Refresh Tokens) | ✅ Accepted |
| [ADR-013](#adr-013-constructor-injection-only) | Constructor Injection Only | ✅ Accepted |
| [ADR-014](#adr-014-slf4j--logback-with-structured-logging) | SLF4J + Logback with Structured Logging | ✅ Accepted |
| [ADR-015](#adr-015-postgresql) | PostgreSQL | ✅ Accepted |
| [ADR-016](#adr-016-spring-ai-for-llm-integration) | Spring AI for LLM Integration | ✅ Accepted |
| [ADR-017](#adr-017-redis-for-caching) | Redis for Caching | ✅ Accepted |
| [ADR-018](#adr-018-websocket-stomp-for-real-time) | WebSocket (STOMP) for Real-Time | ✅ Accepted |
| [ADR-019](#adr-019-api-versioning-via-url-path) | API Versioning via URL Path | ✅ Accepted |
| [ADR-020](#adr-020-hard-delete-by-default) | Hard Delete by Default | ✅ Accepted |

---

## ADR-001: Monolith-First Architecture

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

We are building an AI-Powered Learning Management System that may eventually need to scale individual components independently via microservices. However, the team is small, the domain boundaries are still being discovered, and operational complexity must be minimized during the initial build phase.

### Decision

Start with a **well-structured modular monolith** using feature-based packaging. Each feature module (`user`, `course`, `lesson`, `quiz`, `ai`, etc.) is self-contained with its own controller, service, repository, DTOs, and mapper. This makes future extraction to microservices straightforward — each package maps directly to a potential microservice boundary.

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Microservices from day one** | Too complex for initial development. Operational overhead (service discovery, API gateways, distributed tracing, inter-service communication) is disproportionate to current team size and feature set. Premature decomposition risks incorrect service boundaries. |

### Consequences

- ✅ Faster development velocity with a single deployable unit.
- ✅ Simpler deployment pipeline (one artifact, one CI/CD flow).
- ✅ Simpler debugging — single process, single log stream, no network hops.
- ⚠️ Cross-module communication via direct service injection now; will transition to API calls / messaging in the microservice phase.
- ⚠️ Must maintain strict module boundaries with discipline — no circular dependencies, no reaching into another module's repository layer.

---

## ADR-002: Feature-Based Package Structure

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

The system comprises 15+ modules (user, course, lesson, enrollment, quiz, AI tutor, notifications, etc.). The package structure must scale to this complexity while keeping individual features easy to locate, understand, and eventually extract.

### Decision

Organize code by **business feature** (`user/`, `course/`, `quiz/`, `ai/`) rather than by technical layer (`controllers/`, `services/`, `repositories/`). Each feature package is self-contained with all its layers:

```
com.lms/
├── user/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── mapper/
├── course/
│   ├── controller/
│   ├── ...
├── common/           ← cross-cutting: config, security, exception
```

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Layer-based packaging** (`controllers/`, `services/`, `repositories/`) | Leads to shotgun surgery — a single feature change touches files across many packages. Hard to reason about a single business capability. Harder to extract into microservices since related code is scattered. |

### Consequences

- ✅ Better cohesion — everything about a feature lives together.
- ✅ Easier navigation — find the feature folder, see all related code.
- ✅ Simpler new developer onboarding.
- ✅ Natural microservice extraction boundary.
- ⚠️ Cross-cutting concerns (config, security, exception handling) placed in shared `common/` packages.

---

## ADR-003: UUID Primary Keys

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

We need a consistent primary key strategy across all entities. The choice impacts security (predictability), scalability (distributed ID generation), and performance (index size).

### Decision

Use **UUID v4** for all entity primary keys via `@GeneratedValue(strategy = GenerationType.UUID)`.

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Auto-increment `Long`** | Leaks entity count to clients, problematic in distributed/multi-database systems, creates IDOR (Insecure Direct Object Reference) attack surface. |
| **ULID** | Sortable and compact, but less standardized across libraries and tooling. |
| **Snowflake IDs** | Requires a coordination service (e.g., Zookeeper) for node ID assignment — unnecessary complexity for a monolith. |

### Consequences

- ✅ No sequential guessing — prevents enumeration attacks.
- ✅ Merge-friendly — IDs generated independently without coordination.
- ✅ Future-proof for microservices (no central sequence).
- ⚠️ Slightly larger indexes (16 bytes vs 8 bytes for `Long`).
- ⚠️ No natural ordering — use `createdAt` for chronological sorting.

---

## ADR-004: BaseEntity with JPA Auditing

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

Every entity in the system requires common audit fields: who created it, when, who last modified it, and when. Repeating these fields and their management logic across 15+ entities is error-prone and violates DRY.

### Decision

Create a `@MappedSuperclass` **BaseEntity** that all entities extend:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
```

An `AuditorAware<String>` bean extracts the current user from `SecurityContextHolder`.

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Manually set timestamps in services** | Repetitive, error-prone, easy to forget in new entities. |
| **Hibernate `@CreationTimestamp` / `@UpdateTimestamp`** | Hibernate-specific (not JPA standard), no support for user tracking (`createdBy`/`updatedBy`). |

### Consequences

- ✅ Consistent auditing across all entities with zero per-entity effort.
- ✅ `AuditorAware` integrates cleanly with Spring Security.
- ✅ Single place to add new common fields (e.g., `version` for optimistic locking).
- ⚠️ Requires `@EnableJpaAuditing` configuration.

---

## ADR-005: Service Interface + Implementation Pattern

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

The service layer is the primary place for business logic. We need a consistent design approach that supports testability, clear contracts, and aligns with enterprise Java conventions.

### Decision

Every service uses an **interface + implementation** pattern:

```
UserService          (interface — contract)
UserServiceImpl      (class — implementation)
```

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Concrete classes only** | Simpler, but loses explicit contract definition. Violates the Dependency Inversion Principle in its strict interpretation. Makes swapping implementations (e.g., mock vs real for testing) less clean. |

### Consequences

- ✅ Clear API contracts — the interface is the service's public contract.
- ✅ Easy mocking in unit tests (mock the interface, not the class).
- ✅ Consistent codebase — every developer follows the same pattern.
- ✅ Demonstrates enterprise convention (valuable for interview preparation and team alignment).
- ⚠️ Slight boilerplate overhead — acceptable given the benefits.

---

## ADR-006: MapStruct for Object Mapping

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

Entities must never be exposed directly via APIs. We need a reliable, performant strategy for converting between JPA entities and DTOs across 15+ modules.

### Decision

Use **MapStruct** for all entity ↔ DTO conversions. MapStruct generates mapping code at compile time via annotation processing.

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(CreateUserRequest request);
}
```

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **ModelMapper** | Uses runtime reflection — slower, type-unsafe, errors discovered at runtime, not compile time. |
| **Manual mapping** | Error-prone, significant boilerplate, easy to miss fields on entity changes. |
| **BeanUtils.copyProperties** | Shallow copy only, no type conversion, no nested mapping, silent failures on mismatched fields. |

### Consequences

- ✅ Type-safe — mapping errors are caught at compile time.
- ✅ Zero runtime overhead — generated code is plain getter/setter calls.
- ✅ Refactor-safe — field renames cause compile errors, not silent bugs.
- ⚠️ Requires annotation processor setup in `pom.xml` (Lombok + MapStruct ordering matters).
- ⚠️ Mapper interfaces are boilerplate, but minimal and declarative.

---

## ADR-007: Separate Request/Response DTOs

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

API input shapes (what a client sends) differ from output shapes (what the API returns). Create requests don't include `id` or `createdAt`; responses shouldn't include `password`. We need a clean separation.

### Decision

Use **separate DTO classes** for each direction:

```
CreateUserRequest    — fields for creation (+ validation annotations)
UpdateUserRequest    — fields for update (subset, different validations)
UserResponse         — fields for API output (no sensitive data)
```

Entities are **never exposed directly** in API responses.

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Single shared DTO** | Conflates create/update/response concerns. Leads to `@JsonIgnore` spaghetti and confusion about which fields are used where. |
| **`@JsonIgnore` on entity fields** | Fragile — couples persistence model to presentation. A JPA column rename or addition can accidentally leak data. |

### Consequences

- ✅ Clean separation of concerns — each DTO has a single responsibility.
- ✅ Independent evolution — API response shape can change without touching entity or request.
- ✅ Validation annotations only on request DTOs (where input enters the system).
- ⚠️ More classes per feature — acceptable trade-off for clarity and safety.

---

## ADR-008: Generic ApiResponse Wrapper

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

Without a consistent response envelope, clients must handle different response shapes for success, error, and validation failure scenarios. Frontend developers need a predictable contract.

### Decision

Wrap all API responses in a generic **`ApiResponse<T>`** wrapper:

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private LocalDateTime timestamp;
    private String path;

    // Static factories
    public static <T> ApiResponse<T> success(T data, String message) { ... }
    public static <T> ApiResponse<T> created(T data, String message) { ... }
    public static <T> ApiResponse<T> error(String message, List<String> errors) { ... }
    public static <T> ApiResponse<T> validationError(List<String> errors) { ... }
}
```

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Raw entities/DTOs as response body** | Inconsistent error format — Spring's default error body differs from success body. Clients can't parse uniformly. |
| **ProblemDetail (RFC 7807)** | Good for errors, but less flexible for wrapping success responses. Doesn't carry generic `data` payload naturally. |

### Consequences

- ✅ Consistent client-side parsing — every response follows the same shape.
- ✅ Structured errors — validation failures return field-level messages.
- ✅ Metadata included (`timestamp`, `path`) for debugging and logging.
- ⚠️ Slightly more verbose controller return statements — mitigated by static factories.

---

## ADR-009: Global Exception Handling with @RestControllerAdvice

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

Without centralized exception handling, every controller must individually catch exceptions and format error responses. This leads to inconsistent error formats, duplicated try-catch blocks, and missed edge cases.

### Decision

Implement a **`GlobalExceptionHandler`** using `@RestControllerAdvice`. Define a custom exception hierarchy:

| Exception | HTTP Status | Usage |
|-----------|-------------|-------|
| `ResourceNotFoundException` | 404 | Entity not found by ID |
| `DuplicateResourceException` | 409 | Unique constraint violation (e.g., duplicate email) |
| `BadRequestException` | 400 | Invalid input beyond validation |
| `BusinessException` | 422 | Business rule violation |

All exceptions are caught and returned as `ApiResponse` objects.

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Try-catch in each controller** | Repetitive, inconsistent error format, easy to miss exception types. |

### Consequences

- ✅ Zero error handling logic in controllers — they only handle the happy path.
- ✅ Consistent error response format across the entire API.
- ✅ Easy to add new exception types as the system grows.
- ✅ Single place to add cross-cutting error concerns (e.g., logging, alerting).

---

## ADR-010: Spring Profiles (dev, test, prod)

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

Different environments require different configurations: database URLs, logging levels, feature flags, external service endpoints. We need a clean way to manage these variations without code changes.

### Decision

Use **three Spring profiles**: `dev` (default), `test`, and `prod`.

```
application.yml          ← common/shared config
application-dev.yml      ← local dev (H2/local PG, debug logging)
application-test.yml     ← integration tests (testcontainers, test DB)
application-prod.yml     ← production (env vars for secrets, JSON logging)
```

**Production secrets** are injected via environment variables (`${DB_URL}`, `${JWT_SECRET}`) — never committed to source control.

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Single config with conditional properties** | Messy, error-prone, hard to audit which config applies where. |
| **External config server (Spring Cloud Config)** | Premature for a monolith. Adds infrastructure dependency without sufficient benefit at this scale. |

### Consequences

- ✅ Clean separation of environment concerns.
- ✅ Environment-specific tuning (connection pools, cache TTLs, log levels).
- ✅ Secrets never in source control.
- ⚠️ Production configuration requires proper environment variable management (Docker env files, Kubernetes secrets, etc.).

---

## ADR-011: Hibernate DDL-Auto Now, Flyway Later

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

During early development (Sprints 1–17), the domain model is evolving rapidly. Entity fields, relationships, and tables change frequently. We need schema management that doesn't slow down iteration.

### Decision

Use **`ddl-auto: update`** during Sprints 1–17 for rapid schema evolution. Transition to **Flyway** from Sprint 20 onward when the schema stabilizes and production deployment begins.

```yaml
# Sprints 1-17
spring.jpa.hibernate.ddl-auto: update

# Sprint 20+
spring.jpa.hibernate.ddl-auto: validate
spring.flyway.enabled: true
```

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Flyway from day one** | Adds overhead during rapid iteration — every entity change requires a migration script. Slows development velocity when the schema is still being discovered. |
| **Liquibase** | XML-based by default, more verbose than Flyway's SQL migrations. Less intuitive for a Java-centric team. |

### Consequences

- ✅ Maximum iteration speed during early development.
- ✅ Flyway provides versioned, auditable, repeatable migrations for production.
- ⚠️ Schema drift risk during `ddl-auto: update` phase — Hibernate won't drop columns or rename tables.
- ⚠️ Transition requires generating baseline Flyway migration from the final `ddl-auto` schema.

---

## ADR-012: JWT Authentication (Access + Refresh Tokens)

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

The LMS API is consumed by web and mobile clients. We need stateless authentication that scales horizontally, supports role-based access control, and doesn't require server-side session storage.

### Decision

Implement **JWT-based authentication** with a dual-token strategy:

| Token | Lifetime | Purpose |
|-------|----------|---------|
| **Access Token** | 15 minutes | API authorization — sent in `Authorization: Bearer` header |
| **Refresh Token** | 7 days | Obtain new access tokens without re-login |

JWT claims include: `userId`, `email`, `roles`.

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Session-based (HttpSession)** | Stateful — requires sticky sessions or shared session store. Scaling complexity. |
| **OAuth2 / OpenID Connect** | Overkill for a monolith with its own user store. Can be layered on later. |
| **API keys** | No user context, no expiration by default, no role-based access. |

### Consequences

- ✅ Stateless — no server-side session storage.
- ✅ Horizontally scalable — any instance can validate tokens.
- ✅ Short-lived access tokens minimize damage window if compromised.
- ✅ Refresh tokens provide seamless UX without frequent re-login.
- ⚠️ True token revocation (e.g., on password change or logout) requires a Redis-backed blacklist for access tokens.
- ⚠️ JWT payload is base64-encoded, not encrypted — don't include sensitive data.

---

## ADR-013: Constructor Injection Only

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

Spring supports three DI styles: field injection (`@Autowired` on fields), setter injection, and constructor injection. The team needs a consistent, testable approach.

### Decision

Use **constructor injection exclusively**. Leverage Lombok's `@RequiredArgsConstructor` for brevity. The `@Autowired` annotation is **never used** on fields.

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;   // injected via constructor
    private final UserMapper userMapper;            // injected via constructor
}
```

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Field injection (`@Autowired`)** | Hidden dependencies — class doesn't advertise what it needs. Impossible to instantiate without a Spring container (hinders unit testing). Allows circular dependencies to go undetected. |
| **Setter injection** | Dependencies become mutable — can be changed after construction. Doesn't enforce required dependencies. |

### Consequences

- ✅ Immutable dependencies — set once at construction, never changed.
- ✅ Fully testable without Spring — just call `new Service(mockRepo, mockMapper)`.
- ✅ Explicit dependency graph — constructor signature is the contract.
- ✅ Circular dependencies cause a startup error (fail-fast), not a runtime NPE.
- ⚠️ Long constructors for services with many dependencies — consider if the class has too many responsibilities (SRP check).

---

## ADR-014: SLF4J + Logback with Structured Logging

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

Effective logging is critical for debugging, monitoring, and auditing. The logging framework must integrate with Spring Boot, support structured output for log aggregation tools, and enable request correlation across components.

### Decision

Use **SLF4J** as the logging facade with **Logback** as the backend (Spring Boot's default).

| Environment | Output Format |
|-------------|--------------|
| **dev** | Human-readable console output |
| **prod** | Structured JSON (parseable by ELK, CloudWatch, Datadog) |

Use **MDC (Mapped Diagnostic Context)** for correlation IDs — injected via a servlet filter on every request.

**Log level conventions:**

| Level | Usage |
|-------|-------|
| `INFO` | Business events (user registered, course published) |
| `DEBUG` | Flow tracing (entering service method, query results) |
| `WARN` | Recoverable issues (cache miss, retry attempt) |
| `ERROR` | Unrecoverable failures (DB connection lost, external API down) |

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Log4j2** | Faster async logging, but Logback is Spring Boot's default — switching adds configuration complexity without proportional benefit. |
| **`System.out.println`** | No levels, no formatting, no MDC, no structured output. Not acceptable for any environment. |

### Consequences

- ✅ Zero-config with Spring Boot — Logback works out of the box.
- ✅ Structured JSON logs are directly parseable by ELK/CloudWatch/Datadog.
- ✅ Correlation IDs enable end-to-end request tracing.
- ✅ MDC context propagates automatically through Spring's thread model.
- ⚠️ Async logging (if enabled) can lose logs on JVM crash — acceptable risk with proper shutdown hooks.

---

## ADR-015: PostgreSQL

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

The LMS requires a relational database with strong consistency, rich querying, JSON support (for flexible fields like quiz options), and vector search capabilities (for RAG-based AI features).

### Decision

Use **PostgreSQL 16+** as the primary database.

| Feature | Usage |
|---------|-------|
| **Relational core** | Users, courses, enrollments, grades |
| **JSONB columns** | Quiz options, dynamic form fields, AI prompt metadata |
| **PGVector extension** | Vector embeddings for RAG (Retrieval-Augmented Generation) |
| **Full-text search** | Course and content search without external search engine |

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **MySQL** | Weaker JSON support, no native vector search extension, less feature-rich for complex queries. |
| **MongoDB** | Document store paradigm mismatches the fundamentally relational nature of LMS data (users → enrollments → courses → lessons). Joins are unnatural. |
| **H2** | In-memory only — suitable for dev/test, not production. |

### Consequences

- ✅ Rich feature set covers relational, document, and vector workloads in a single engine.
- ✅ Strong ecosystem — tooling, hosting, community, managed services (RDS, Cloud SQL, Supabase).
- ✅ JSONB provides schema flexibility where needed without sacrificing relational integrity.
- ⚠️ PostgreSQL-specific features (JSONB operators, PGVector) reduce database portability — acceptable trade-off given no planned database switch.

---

## ADR-016: Spring AI for LLM Integration

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

The LMS includes AI-powered features: intelligent tutoring, content summarization, quiz generation, and RAG-based Q&A over course materials. We need a framework to integrate with LLM providers (OpenAI, Google Gemini) in a Spring-native way.

### Decision

Use **Spring AI** as the LLM integration framework.

| Capability | Spring AI Feature |
|-----------|-------------------|
| **Provider abstraction** | `ChatClient` interface — swap OpenAI ↔ Gemini via config |
| **Prompt management** | `PromptTemplate` with variable substitution |
| **Structured output** | `BeanOutputConverter` — LLM response → Java object |
| **Vector store** | `VectorStore` interface with PGVector implementation |
| **Function calling** | `@Description` annotated functions as tools for the LLM |

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Direct HTTP calls to OpenAI/Gemini** | No abstraction — provider switch requires rewriting integration code. No prompt management, no structured output parsing. |
| **LangChain4j** | Capable library, but less Spring-native. Doesn't integrate as naturally with Spring Boot auto-configuration, dependency injection, and the broader Spring ecosystem. |

### Consequences

- ✅ Clean Spring Boot integration — auto-configuration, dependency injection, profiles.
- ✅ Easy provider switching — change `spring.ai.openai` to `spring.ai.vertex` in config.
- ✅ Structured output parsing reduces brittle regex/JSON parsing of LLM responses.
- ⚠️ Spring AI is relatively newer — API may evolve. Backed by the Spring team, mitigating abandonment risk.
- ⚠️ Must handle LLM latency (2–10s per call) — async processing and streaming responses are essential.

---

## ADR-017: Redis for Caching

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

Certain data is read frequently but changes infrequently: course catalogs, user profiles, category lists, leaderboards. Database queries for these on every request are wasteful. We need a caching layer.

### Decision

Use **Redis** via Spring Data Redis with Spring's `@Cacheable` / `@CacheEvict` abstraction.

**Cached data:**

| Data | TTL | Eviction Trigger |
|------|-----|-------------------|
| Course details | 30 min | Course update |
| User profiles | 15 min | Profile update |
| Category lists | 1 hour | Category CRUD |
| Leaderboard | 5 min | Score update |

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Caffeine (local cache)** | In-process only — not shared across application instances. Cache inconsistency in multi-instance deployments. |
| **EhCache** | Older, less community activity. Distributed mode (Terracotta) adds complexity. |
| **Hazelcast** | Full-featured distributed data grid — heavier than needed for a caching layer. |

### Consequences

- ✅ Shared cache across all application instances — consistent data.
- ✅ Optional persistence (RDB/AOF) — cache survives restarts.
- ✅ Spring's `@Cacheable` annotation makes caching declarative — minimal code changes.
- ⚠️ Requires Redis infrastructure (managed service or container).
- ⚠️ Must implement graceful fallback to database on Redis failure — cache is an optimization, not a dependency.
- ⚠️ Cache invalidation must be correct — stale data is worse than no cache.

---

## ADR-018: WebSocket (STOMP) for Real-Time

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

The LMS needs real-time features: live notifications (assignment graded, new announcement), real-time quiz participation, and collaborative features. Polling is inefficient for these use cases.

### Decision

Use **WebSocket with STOMP protocol** over SockJS for real-time communication.

| Channel | Purpose |
|---------|---------|
| `/user/queue/notifications` | User-specific notifications (private) |
| `/topic/course/{id}/announcements` | Course-wide broadcasts |
| `/topic/quiz/{id}/live` | Live quiz events |

SockJS provides automatic fallback for environments that don't support WebSocket (corporate proxies, older browsers).

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Server-Sent Events (SSE)** | Unidirectional (server → client only). Doesn't support client-initiated messages. Connection limit per domain in HTTP/1.1. |
| **Long polling** | Inefficient — constant connection setup/teardown. High latency. Server resource waste. |
| **Firebase Cloud Messaging (FCM)** | Mobile-only push notifications. Not suitable for web real-time features. |

### Consequences

- ✅ Bi-directional communication — both server and client can send messages.
- ✅ Spring-native — `@MessageMapping`, `SimpMessagingTemplate`, built-in broker.
- ✅ STOMP provides structure (destinations, subscriptions, headers) over raw WebSocket.
- ✅ SockJS fallback ensures broad compatibility.
- ⚠️ Requires WebSocket-compatible infrastructure — load balancers/proxies must support WS upgrade.
- ⚠️ Connection management at scale — consider external STOMP broker (RabbitMQ) for multi-instance deployments.

---

## ADR-019: API Versioning via URL Path

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

As the LMS API evolves, breaking changes to request/response shapes are inevitable. We need a versioning strategy that allows existing clients to continue functioning while new clients adopt updated APIs.

### Decision

Version the API via **URL path prefix**: `/api/v1/...`

```
/api/v1/users
/api/v1/courses
/api/v1/courses/{id}/lessons
```

Version is incremented **only on breaking changes** (field removal, type change, semantic change). Additive changes (new optional fields, new endpoints) do NOT require a version bump.

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Header versioning (`Accept-Version: v1`)** | Less discoverable — not visible in URL. Harder to test in browser. Requires custom header parsing. |
| **Media type versioning (`Accept: application/vnd.lms.v1+json`)** | Complex content negotiation. Harder for clients to implement. Over-engineered for current needs. |

### Consequences

- ✅ Simple and visible — version is part of the URL, immediately apparent.
- ✅ Easy to test — paste URL in browser or curl, version is right there.
- ✅ Cacheable — different versions are different URLs, cache-friendly.
- ✅ Well-understood — most public APIs use this approach.
- ⚠️ URL changes on version bump — clients must update base URLs.
- ⚠️ Must maintain backward compatibility within a version — only bump on true breaking changes.

---

## ADR-020: Hard Delete by Default

| Field | Detail |
|-------|--------|
| **Status** | Accepted |
| **Date** | 2026-07-11 |

### Context

When a user or admin deletes a resource (course, lesson, quiz), we need to decide whether to physically remove the data (hard delete) or mark it as deleted while retaining it in the database (soft delete).

### Decision

Use **hard delete by default** (YAGNI — You Ain't Gonna Need It). If a business requirement explicitly demands data retention or undo capability for a specific entity, implement soft delete **for that entity only**:

```java
// Only when business explicitly requires it:
@Column(name = "deleted")
private boolean deleted = false;

@Column(name = "deleted_at")
private LocalDateTime deletedAt;
```

### Alternatives Considered

| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Soft delete everything by default** | Ghost data accumulates — deleted records pollute queries. Requires `@SQLRestriction("deleted = false")` or `@Where` on every query. Complicates unique constraints (deleted user with same email blocks re-registration). Performance degrades as deleted records accumulate. |

### Consequences

- ✅ Simpler queries — no `WHERE deleted = false` everywhere.
- ✅ No ghost data — database reflects actual state.
- ✅ Unique constraints work naturally.
- ✅ Simpler data model — fewer columns, less complexity.
- ⚠️ Deletes are irreversible — must be clear in UI ("Are you sure?").
- ⚠️ Can retrofit soft delete per entity if business requirements emerge — add `deleted` flag and a `@SQLRestriction`.
- ⚠️ Consider database backups as the recovery mechanism for accidental deletes.

---

## 📝 How to Add New Decisions

When a new architectural or technical decision is made, follow this process:

### 1. Assign the Next ADR Number

Use the next sequential number (e.g., `ADR-021`).

### 2. Use the Standard Template

```markdown
## ADR-XXX: Title

| Field | Detail |
|-------|--------|
| **Status** | Proposed / Accepted / Deprecated / Superseded by ADR-YYY |
| **Date** | YYYY-MM-DD |

### Context
Why this decision is needed. What problem or question prompted it?

### Decision
What was decided and the key reasoning behind it.

### Alternatives Considered
| Alternative | Reason for Rejection |
|-------------|---------------------|
| **Option A** | Why it was rejected. |
| **Option B** | Why it was rejected. |

### Consequences
- ✅ Positive outcomes
- ⚠️ Trade-offs and risks

---
```

### 3. Status Lifecycle

| Status | Meaning |
|--------|---------|
| **Proposed** | Under discussion, not yet agreed upon |
| **Accepted** | Agreed and in effect |
| **Deprecated** | No longer relevant (e.g., feature removed) |
| **Superseded by ADR-YYY** | Replaced by a newer decision |

### 4. Guidelines

- **One decision per ADR** — don't combine multiple decisions.
- **Be specific** — include code snippets, configuration examples, or diagrams where they clarify the decision.
- **Record alternatives honestly** — future readers need to understand why other options were rejected.
- **Update, don't delete** — if a decision changes, mark the old ADR as superseded and create a new one.
- **Date accurately** — the date reflects when the decision was accepted, not when the ADR was written.
