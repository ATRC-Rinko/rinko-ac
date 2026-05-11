# Module Structure Standards

This specification defines the directory structure, Maven configuration, and module-level conventions for the Rinko project. Requirements are derived from `docs/spec.md` and the actual structure of `rinko-infra` and `rinko-auth`.

---

## ADDED Requirements

### Requirement: Standard Module Directory Layout

Every module SHALL have the following minimum directory structure:

```
{module}/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/          # Java/Kotlin source files
│   │   │   └── com/rinko/{module}/
│   │   │       ├── {sub-package}/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── logback-spring.xml
│   │       └── db/migration/    # Flyway migration scripts (if module has DB)
│   └── test/
│       ├── java/
│       │   └── com/rinko/{module}/
│       └── resources/
│           └── application.yml  # Test-specific config
```

#### Scenario: Creating a new business module

- **WHEN** a developer scaffolds `rinko-oss`
- **THEN** SHALL create the standard directory layout with all required directories
- **AND** `pom.xml` SHALL declare dependency on `rinko-infra` with `<scope>compile</scope>`

---

### Requirement: POM Configuration for Servlet Modules

Servlet modules (`rinko-oss`, `rinko-log`, `rinko-notify`, `rinko-scheduler`) SHALL:

1. Declare `<parent>` as `rinko-ac` (root POM)
2. Exclude `spring-boot-starter-tomcat` from `spring-boot-starter-web`
3. Add `spring-boot-starter-jetty` dependency
4. Declare dependency on `rinko-infra`
5. Declare dependency on `spring-boot-starter-web`
6. NOT include any WebFlux or reactive dependencies

#### Scenario: Validating pom.xml for rinko-oss

- **WHEN** reviewing `rinko-oss/pom.xml`
- **THEN** `<parent>` SHALL reference `com.rinko:rinko-ac`
- **AND** `<exclusions>` SHALL include `spring-boot-starter-tomcat`
- **AND** dependencies SHALL include `rinko-infra`, `spring-boot-starter-web`, `spring-boot-starter-jetty`

---

### Requirement: POM Configuration for WebFlux Modules

WebFlux modules (`rinko-gateway`, `rinko-auth`) SHALL:

1. Declare `<parent>` as `rinko-ac` (root POM)
2. Use `spring-boot-starter-webflux` (not `spring-boot-starter-web`)
3. Declare dependency on `rinko-infra`
4. Use `kotlin-stdlib` and `kotlin-reflect` as compile dependencies
5. NOT include any Servlet container dependencies (Tomcat, Jetty, Undertow)

#### Scenario: Validating pom.xml for rinko-auth

- **WHEN** running `mvn dependency:tree` on `rinko-auth`
- **THEN** `spring-boot-starter-webflux` SHALL be present
- **AND** NO `tomcat-embed-*` or `jetty-*` Servlet artifacts SHALL appear
- **AND** `kotlin-stdlib` SHALL be present

---

### Requirement: Application Entry Point

Every business module SHALL have a main application class annotated with `@SpringBootApplication`.

The main class SHALL be at the root of the module's package (e.g., `com.rinko.auth.RinkoAuthApplication`).

The class name SHALL follow the pattern `Rinko{Module}Application` (e.g., `RinkoAuthApplication`, `RinkoOssApplication`).

WebFlux modules (gateway, auth) SHALL also be annotated with `@EnableDiscoveryClient` for Nacos service registration.

#### Scenario: Creating the main class for rinko-oss

- **WHEN** a developer creates the entry point for `rinko-oss`
- **THEN** the class SHALL be named `RinkoOssApplication`
- **AND** it SHALL be annotated with `@SpringBootApplication`
- **AND** it SHALL reside at `com.rinko.oss.RinkoOssApplication`

---

### Requirement: Module-Specific application.yml

Every module SHALL have `src/main/resources/application.yml` containing at minimum:

- `spring.application.name` set to the module name (e.g., `rinko-auth`)
- `spring.config.import` with Nacos configuration references:
  - `optional:nacos:application-dev.yml` (shared config)
  - `optional:nacos:{module}-dev.yml` (module-specific config)
- `spring.cloud.nacos.discovery.server-addr` with `${NACOS_SERVER:localhost:8848}`
- `spring.cloud.nacos.config.server-addr` with `${NACOS_SERVER:localhost:8848}`
- `server.port` set to the module's assigned port

Port assignments SHALL be:
- `rinko-gateway`: 8080
- `rinko-auth`: 8081
- `rinko-oss`: 8082
- `rinko-log`: 8083
- `rinko-notify`: 8084
- `rinko-scheduler`: 8085

#### Scenario: Creating application.yml for a new module

- **WHEN** creating `rinko-oss/src/main/resources/application.yml`
- **THEN** it SHALL contain `spring.application.name: rinko-oss`
- **AND** SHALL import both `application-dev.yml` and `rinko-oss-dev.yml` from Nacos
- **AND** SHALL set `server.port: 8082`
- **AND** SHALL NOT contain `bootstrap.yml`

---

### Requirement: logback-spring.xml Configuration

Every module SHALL have `src/main/resources/logback-spring.xml` that configures:

1. `TraceIdConverter` from `rinko-infra` as a Logback converter
2. Console appender using `JsonEncoder` from `rinko-infra`
3. File appender using `JsonEncoder` with daily rolling and 30-day retention
4. Root log level set via Spring property `${LOG_LEVEL:INFO}`

#### Scenario: Module starts up in development

- **WHEN** a module starts with default configuration
- **THEN** logs SHALL be output to console in structured JSON format
- **AND** log files SHALL be written to `./logs/{module-name}.log`
- **AND** traceId and spanId SHALL appear in each log entry

---

### Requirement: Graceful Shutdown Configuration

All modules SHALL support graceful shutdown with the following configuration (via Nacos shared config `application-dev.yml`):

```yaml
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
server:
  shutdown: graceful
```

#### Scenario: Module receives SIGTERM during request processing

- **WHEN** SIGTERM is sent to a running module
- **THEN** the module SHALL finish processing in-flight requests before shutting down
- **AND** the shutdown SHALL complete within 30 seconds

---

### Requirement: Dockerfile in Module Directory

Every deployable service module SHALL have a `Dockerfile` at the module root, alongside `pom.xml`.

The Dockerfile SHALL use multi-stage builds (JDK build stage → JRE runtime stage).

`rinko-infra` is a shared library and SHALL NOT have a Dockerfile.

The standard directory layout SHALL include:
```
{module}/
├── pom.xml
├── Dockerfile          # Multi-stage build (service modules only)
├── src/
│   ├── main/
│   └── test/
```

#### Scenario: Creating the rinko-oss service module

- **WHEN** `rinko-oss` module is scaffolded
- **THEN** it SHALL contain a `Dockerfile` at `rinko-oss/Dockerfile`
- **AND** the Dockerfile SHALL use `eclipse-temurin:21-jdk-alpine` for build and `eclipse-temurin:21-jre-alpine` for runtime

#### Scenario: Verifying rinko-infra has no Dockerfile

- **WHEN** checking all modules for Dockerfiles
- **THEN** `rinko-infra/` SHALL NOT contain a `Dockerfile`
- **AND** all other 6 modules SHALL contain a `Dockerfile`
