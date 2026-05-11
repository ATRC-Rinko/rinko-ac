# Coding Standards

This specification defines the coding standards for the Rinko project, applicable to both Java and Kotlin modules. All requirements are derived from actual patterns observed in `rinko-infra` and `rinko-auth`.

---

## ADDED Requirements

### Requirement: Package Structure Convention

All modules SHALL follow a consistent package structure with domain-separated sub-packages. The root package for each module SHALL be `com.rinko.<module-name>`.

Sub-packages for a business module SHALL include:
- `config` — configuration classes (@Configuration, auto-configuration, properties)
- `controller` — REST controllers (@RestController, @RequestMapping)
- `dto` — data transfer objects (requests, responses, data classes)
- `entity` — persistent entities (@Table-annotated data classes)
- `repository` — data access interfaces extending ReactiveCrudRepository or JpaRepository
- `service` — business logic interfaces and implementations
- `security` — authentication/authorization components (JWT, filters)
- `event` — Spring application event classes
- `listener` — event listener classes
- `cache` — caching service classes

Infrastructure modules (rinko-infra) MAY use additional sub-packages:
- `web` — web layer components (filters, interceptors, CORS)
- `log` — logging components (encoders, converters)
- `id` — ID generation utilities
- `datasource` — data source routing
- `flyway` — database migration support
- `exception` — exception model classes

#### Scenario: New Servlet business module created

- **WHEN** a developer creates a new Java business module (e.g., `rinko-oss`)
- **THEN** the root package SHALL be `com.rinko.oss`
- **AND** it SHALL contain at minimum `config`, `controller`, `dto`, `entity`, `repository`, `service` sub-packages

#### Scenario: New WebFlux business module created

- **WHEN** a developer creates a new Kotlin WebFlux module
- **THEN** the root package SHALL be `com.rinko.<module>` (e.g., `com.rinko.auth`)
- **AND** it SHALL additionally include `security`, `event`, `listener`, `cache` sub-packages as applicable

---

### Requirement: Exception Model — RinkoException Hierarchy

All custom exceptions SHALL extend `RinkoException`, the project's abstract base exception class.

`RinkoException` SHALL provide:
- `errorCode` — a machine-readable string identifier (e.g., `"VALIDATION_ERROR"`, `"NOT_FOUND"`)
- `errorMessage` — a human-readable message
- `httpStatus` — the corresponding HTTP status code from `org.springframework.http.HttpStatus`

Five standard exception subclasses SHALL be used:
- `ValidationException` — errorCode `"VALIDATION_ERROR"`, HTTP 400
- `NotFoundException` — errorCode `"NOT_FOUND"`, HTTP 404
- `UnauthorizedException` — errorCode `"UNAUTHORIZED"`, HTTP 401
- `ForbiddenException` — errorCode `"FORBIDDEN"`, HTTP 403
- `InternalException` — errorCode `"INTERNAL_ERROR"`, HTTP 500

All public methods SHALL use these standard exceptions rather than creating ad-hoc exception types.

#### Scenario: Validation failure in controller

- **WHEN** a controller receives invalid input
- **THEN** the controller SHALL throw `ValidationException` with `errorCode="VALIDATION_ERROR"` and HTTP status 400
- **AND** the error response SHALL be rendered as RFC 7807 ProblemDetail

#### Scenario: Resource not found

- **WHEN** a service cannot find a requested entity by ID
- **THEN** the service SHALL throw `NotFoundException` with `errorCode="NOT_FOUND"` and HTTP status 404

---

### Requirement: Naming Conventions

Class names in Java modules SHALL use standard Java naming (PascalCase classes, camelCase methods/variables).

Class names in Kotlin modules SHALL use Kotlin conventions:
- **Data classes**: Simple noun-based names (e.g., `RegisterRequest`, `TokenPair`, `RolePermission`)
- **Entities**: Noun matching table name (e.g., `User`, `Role`, `Permission`, `OAuth2Client`)
- **Controllers**: `{Domain}Controller` suffix (e.g., `AuthController`, `RoleController`, `PermissionController`)
- **Services**: `{Domain}Service` suffix for interface and `{Domain}ServiceImpl` for implementation
- **Repositories**: `{Entity}Repository` suffix extending `ReactiveCrudRepository`
- **Events**: `{Domain}{Action}Event` format (e.g., `PermissionChangedEvent`, `UserRoleChangedEvent`)

Shared DTOs in `rinko-infra` SHALL have descriptive names indicating their purpose:
- `PageRequest`, `PageResponse<T>` — pagination
- `ProblemDetail` — error response
- `SortOrder` — sorting specification

#### Scenario: New service interface for file management

- **WHEN** a developer creates a service for file management in `rinko-oss`
- **THEN** the interface SHALL be named `FileService` and the implementation `FileServiceImpl`

#### Scenario: New event for role assignment

- **WHEN** a developer publishes an event when a role is assigned to a user
- **THEN** the event class SHALL be named `RoleAssignedEvent` and placed in the `event` sub-package

---

### Requirement: Code Documentation

All public methods SHALL have a documentation comment explaining their purpose.

Entity classes SHALL have class-level Javadoc/KDoc describing the table and domain concept.

Controller endpoints SHALL have `@Operation` (SpringDoc) annotations with a `summary` field.

Configuration classes SHALL document which application properties they bind to, using `@ConfigurationProperties` with a clear prefix.

#### Scenario: New public service method

- **WHEN** a developer adds a public method to a service class
- **THEN** the method SHALL have a Javadoc/KDoc comment explaining its purpose
- **AND** the comment SHALL include `@param` and `@return` tags where applicable

#### Scenario: New REST endpoint

- **WHEN** a developer adds a new endpoint to a controller
- **THEN** the method SHALL be annotated with `@Operation(summary = "...")`
- **AND** the controller class SHALL be annotated with `@Tag(name = "...")`

---

### Requirement: Logging Standards

All modules SHALL use SLF4J for logging (not direct Logback/log4j API calls).

The `JsonEncoder` from `rinko-infra` SHALL be the standard log encoder configured via `logback-spring.xml`.

Log output SHALL be structured JSON with the following fields:
- `timestamp` — ISO 8601 with timezone offset
- `level` — log level (INFO, WARN, ERROR, DEBUG)
- `service` — service name (from `spring.application.name`)
- `traceId` — SkyWalking trace ID (or `"N/A"` if SkyWalking unavailable)
- `spanId` — SkyWalking span ID (or `"N/A"` if unavailable)
- `class` — fully qualified class name
- `message` — log message
- `thread` — thread name
- `context` — MDC context map (key-value pairs)

Sensitive data SHALL NOT appear in logs. Fields annotated with `@Sensitive` SHALL be automatically masked to `"***"` by the JSON encoder.

#### Scenario: Business operation log

- **WHEN** a service method logs a successful user login event
- **THEN** the log output SHALL be a valid JSON object
- **AND** SHALL contain `"level": "INFO"`, the service name, traceId, and a non-empty message
- **AND** SHALL NOT contain password or token values in plaintext

#### Scenario: SkyWalking unavailable in local development

- **WHEN** SkyWalking agent is not attached
- **THEN** the log JSON SHALL contain `"traceId": "N/A"` and `"spanId": "N/A"`
- **AND** the application SHALL continue to function normally

---

### Requirement: Input Sanitization

All user input SHALL be sanitized before processing.

The `XssFilter` from `rinko-infra` SHALL be registered in all Servlet-based modules. It SHALL escape HTML special characters: `< > & " '`.

WebFlux modules SHALL use Spring Security's built-in header and parameter filtering.

#### Scenario: Malicious script tag in request parameter

- **WHEN** a user submits a request parameter containing `<script>alert(1)</script>`
- **THEN** the XssFilter SHALL escape the value to `&lt;script&gt;alert(1)&lt;/script&gt;`
- **AND** the original script SHALL NOT execute

---

### Requirement: ID Generation

All entity primary keys SHALL be generated using `SnowflakeIdGenerator` from `rinko-infra`.

Snowflake ID properties SHALL be:
- Worker ID: automatically assigned from MAC address hash, with SecureRandom fallback
- Timestamp precision: millisecond-level
- Output: globally unique Long

#### Scenario: Creating a new user entity

- **WHEN** a new user is created
- **THEN** the user's ID SHALL be generated via `SnowflakeIdGenerator.nextId()`
- **AND** the resulting Long SHALL be unique within the distributed system

---

### Requirement: WebFlux vs Servlet Module Conventions

Servlet modules (Java: `rinko-oss`, `rinko-log`, `rinko-notify`, `rinko-scheduler`) SHALL:
- Exclude `spring-boot-starter-tomcat`
- Include `spring-boot-starter-jetty`
- Use standard Spring MVC annotations (@RestController, @GetMapping, etc.)
- Use blocking JDBC for database access

WebFlux modules (Kotlin: `rinko-gateway`, `rinko-auth`) SHALL:
- Use Netty as the embedded server (default for WebFlux)
- NOT introduce any Servlet container dependencies
- Use reactive programming patterns (Mono/Flux, ReactiveCrudRepository)
- Use `SecurityWebFilterChain` for security configuration
- Use R2DBC for reactive database access

#### Scenario: Servlet module dependency validation

- **WHEN** running `mvn dependency:tree` on `rinko-oss`
- **THEN** `spring-boot-starter-tomcat` SHALL NOT appear in the dependency tree
- **AND** `spring-boot-starter-jetty` SHALL appear

#### Scenario: WebFlux module dependency validation

- **WHEN** running `mvn dependency:tree` on `rinko-auth`
- **THEN** no Servlet API or Tomcat/Jetty dependencies SHALL appear
- **AND** `spring-boot-starter-webflux` SHALL appear

---

### Requirement: Kotlin All-Open Compiler Plugin

All Kotlin modules SHALL configure the `kotlin-allopen` compiler plugin in the `kotlin-maven-plugin` to ensure Spring-managed classes are `open` for CGLIB proxying.

The `kotlin-allopen` plugin SHALL be configured at the root POM level (`<pluginManagement>`) so all Kotlin modules inherit it automatically.

The plugin SHALL declare `open` behavior for at minimum the following Spring annotations:
- `org.springframework.stereotype.Component`
- `org.springframework.stereotype.Service`
- `org.springframework.stereotype.Repository`
- `org.springframework.transaction.annotation.Transactional`
- `org.springframework.context.annotation.Configuration`
- `org.springframework.web.bind.annotation.RestController`

#### Scenario: Class annotated with @Service is automatically open

- **WHEN** a Kotlin class is annotated with `@Service`
- **THEN** the compiled bytecode SHALL have the class as `open` (not `final`)
- **AND** Spring CGLIB proxying SHALL work without requiring an explicit `open` keyword

#### Scenario: Class annotated with @Transactional has open methods

- **WHEN** a Kotlin class is annotated with `@Transactional`
- **THEN** the compiled bytecode SHALL have all methods as `open`
- **AND** Spring transaction proxy SHALL properly intercept transactional method calls

#### Scenario: Class without Spring annotations remains final

- **WHEN** a Kotlin class has no Spring annotations
- **THEN** the compiled bytecode SHALL have the class as `final` (Kotlin default)
- **AND** the allopen plugin SHALL NOT change non-Spring classes

#### Scenario: Compilation after clean

- **WHEN** `mvn clean compile` is executed on a Kotlin module
- **THEN** the `kotlin-allopen` plugin SHALL process all source files
- **AND** compilation SHALL succeed without errors

---

### Requirement: Java Data Class Selection — Record vs Lombok

Java modules SHALL use **Java `record`** for immutable DTOs and **Lombok** for mutable Java Beans.

**Record** SHALL be used when all fields are final and no setters are needed (e.g., Kafka message DTOs, query response DTOs).

**Lombok `@Data`** SHALL be used when mutable state is required (e.g., JDBC-mapped entities, Spring Data entities).

**Lombok `@Getter` + `@Setter`** SHALL be used for `@ConfigurationProperties` classes where Spring injects values via setters.

The project SHALL include a `lombok.config` at the root with `lombok.addLombokGeneratedAnnotation = true` to exclude generated code from JaCoCo.

Lombok SHALL be declared with `<scope>provided</scope>`.

#### Scenario: Creating a new immutable DTO

- **WHEN** a developer creates a new DTO that carries data without mutation
- **THEN** the DTO SHALL be a Java `record`
- **AND** no explicit constructor, getter, equals, hashCode, or toString SHALL be written

#### Scenario: Creating a new JDBC-mapped entity

- **WHEN** a developer creates a new JDBC-mapped entity needing setters
- **THEN** the class SHALL use Lombok `@Data`
- **AND** no explicit getters or setters SHALL be written

#### Scenario: Creating a configuration properties class

- **WHEN** a developer creates a `@ConfigurationProperties` class
- **THEN** the class SHALL use `@Getter` and `@Setter`
- **AND** Spring Boot binding SHALL work via generated setters

---

### Requirement: JDK 21 Feature Usage

Java modules SHALL leverage JDK 21 finalized features:

- **Virtual Threads**: Servlet modules (Java, Jetty) SHALL enable Virtual Threads via `spring.threads.virtual.enabled=true`. WebFlux modules (Kotlin, Netty) SHALL continue using their reactive thread model.

- **Record**: Immutable DTOs SHALL use Java `record`.

- **Pattern Matching**: `instanceof` pattern matching SHALL be used to eliminate redundant casts.

- **Sequenced Collections**: `getFirst()`/`getLast()` from `SequencedCollection` SHOULD be used where clearer than `get(0)`/`get(size()-1)`.

Virtual Threads SHALL NOT be used for `synchronized`-heavy code paths, as they pin the carrier thread.

#### Scenario: Servlet module request handling on Virtual Threads

- **WHEN** `spring.threads.virtual.enabled=true` is set in Nacos shared config
- **THEN** Jetty SHALL use virtual threads for request processing
- **AND** the module SHALL handle high concurrency without platform thread pool exhaustion

#### Scenario: Using pattern matching for type-safe cast

- **WHEN** a developer writes conditional type checking
- **THEN** they SHALL use `instanceof` pattern matching
- **AND** SHALL NOT write `(String) obj` explicit cast after `instanceof` check

---

### Requirement: Unified API Response Format

All REST API endpoints SHALL return responses wrapped in `ApiResponse<T>` from `rinko-infra`.

Successful responses SHALL use `ApiResponse.success(data)` with HTTP 2xx.

`RinkoException` and its subclasses SHALL be automatically handled by the global exception handler — controllers SHALL NOT catch them manually.

The response format SHALL be consistent across Servlet (Java) and WebFlux (Kotlin) modules via two separate `@RestControllerAdvice` handlers:
- `GlobalExceptionHandler` — Servlet modules, sets status via `HttpServletResponse`
- `ReactiveGlobalExceptionHandler` — WebFlux modules, sets status via `ServerWebExchange`

#### Scenario: Successful response

- **WHEN** a controller returns `ApiResponse.success(someData)`
- **THEN** the response body SHALL be `{"code":200,"message":"OK","data":{...},"timestamp":"..."}`

#### Scenario: Validation exception

- **WHEN** a `ValidationException` is thrown from the service layer
- **THEN** the handler SHALL return `ApiResponse.error(400, "...")` without `ResponseEntity` wrapping
- **AND** HTTP status SHALL be 400

---

### Requirement: Distributed Transaction with @GlobalTransactional

Any business operation spanning multiple microservices SHALL use Seata `@GlobalTransactional` annotation.

The annotation SHALL be placed on the initiating service method. `timeoutMills` (default 60000ms) and `name` SHALL be set for traceability.

Single-service operations SHALL use local `@Transactional` only.

#### Scenario: Cross-service distributed transaction

- **WHEN** a service method calls two different microservices writing to separate databases
- **THEN** the method SHALL be annotated with `@GlobalTransactional`
- **AND** both databases SHALL have `undo_log` table for automatic rollback

#### Scenario: Single-service local transaction

- **WHEN** a method only accesses one database within a single service
- **THEN** `@GlobalTransactional` SHALL NOT be used
- **AND** `@Transactional` SHALL be sufficient
