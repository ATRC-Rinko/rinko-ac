# Configuration Management Standards

This specification defines configuration management standards for the Rinko project. Requirements are derived from the actual Nacos configuration setup in the project, the `application.yml` files in each module, and the conventions documented in `docs/spec.md`.

---

## ADDED Requirements

### Requirement: Nacos as Central Configuration Source

All modules SHALL use Nacos as the central configuration management system.

Nacos discovery and config SHALL be configured directly in `application.yml` (NOT in `bootstrap.yml`).

The `spring.config.import` property SHALL be used to import Nacos configurations:
```yaml
spring:
  config:
    import:
      - optional:nacos:application-dev.yml
      - optional:nacos:{service-name}-dev.yml
```

The `optional:` prefix SHALL be used to prevent startup failure when Nacos is unreachable (local development).

#### Scenario: Nacos unavailable during local development

- **WHEN** a module starts locally without Nacos running
- **THEN** the module SHALL start successfully with only local `application.yml` values
- **AND** SHALL log a warning about Nacos unavailability

#### Scenario: Nacos configuration propagation

- **WHEN** a configuration value is modified in Nacos
- **THEN** all running services SHALL receive the updated configuration via Spring Cloud Bus
- **AND** no service restart SHALL be required

---

### Requirement: Forbidden Configuration Patterns

`bootstrap.yml` or `bootstrap.properties` files SHALL NOT exist in any module.

`spring.cloud.nacos.config.shared-configs` SHALL NOT be used.

`spring.cloud.nacos.config.extension-configs` SHALL NOT be used.

#### Scenario: Auditing a module for forbidden patterns

- **WHEN** checking a module's configuration files
- **THEN** no file named `bootstrap.yml` or `bootstrap.properties` SHALL exist
- **AND** `shared-configs` SHALL NOT appear in any configuration file

---

### Requirement: Environment Variable-Based Configuration

All environment-specific values (database credentials, API keys, hostnames) SHALL be injected via environment variables with sensible defaults for local development.

Environment variable naming pattern: `${VAR_NAME:default_value}`.

Examples:
- `${DB_HOST:localhost}`
- `${DB_PASSWORD:}`
- `${NACOS_SERVER:localhost:8848}`
- `${REDIS_PASSWORD:}`

Sensitive values (passwords, secrets) SHALL NOT have default values (empty default only).

#### Scenario: Production deployment with real credentials

- **WHEN** deploying to production
- **THEN** `DB_PASSWORD` environment variable SHALL be set to the production database password
- **AND** the application SHALL use the environment-provided value, not any default

#### Scenario: Local development without explicit environment variables

- **WHEN** a developer starts a module locally without setting `DB_HOST`
- **THEN** the module SHALL default to `localhost`

---

### Requirement: Nacos Configuration File Structure

Each module SHALL have a dedicated Nacos configuration file named `{service-name}-dev.yml`.

A shared configuration file `application-dev.yml` SHALL contain configuration common to all services:

Shared configuration (`application-dev.yml`) SHALL include:
- SkyWalking backend addresses
- Graceful shutdown parameters (30s timeout)
- Feign/WebClient timeouts and connection pool settings
- Resilience4j circuit breaker and retry configuration
- Log level defaults
- Actuator/metrics endpoint exposure

Module-specific configuration (`{service-name}-dev.yml`) SHALL include:
- Data source URLs and connection pool settings
- Redis connection details
- RabbitMQ/Kafka bootstrap servers (if applicable)
- JWT secret and expiration settings (for auth)
- Module-specific business configuration

#### Scenario: Creating Nacos configuration for rinko-oss

- **WHEN** setting up Nacos configuration for `rinko-oss`
- **THEN** a file `rinko-oss-dev.yml` SHALL be created in Nacos
- **AND** it SHALL contain rinko-oss-specific PostgreSQL connection details
- **AND** it SHALL contain object storage endpoint configuration
- **AND** it SHALL NOT duplicate settings from `application-dev.yml`

---

### Requirement: Configuration Properties Classes

Complex configuration groups SHALL use `@ConfigurationProperties` classes for type-safe binding.

Properties class naming: `{Feature}Properties` (e.g., `CorsProperties`, `DruidDataSourceProperties`).

Properties prefix SHALL follow the pattern `rinko.{feature}` (e.g., `rinko.cors`, `spring.datasource.druid`).

Properties classes SHALL be registered via `@EnableConfigurationProperties` or `@ConfigurationPropertiesScan`.

#### Scenario: Adding object storage configuration for rinko-oss

- **WHEN** creating OSS configuration properties
- **THEN** the class SHALL be named `OssProperties`
- **AND** SHALL be annotated with `@ConfigurationProperties(prefix = "oss")`
- **AND** SHALL contain fields `endpoint`, `accessKey`, `secretKey`, `bucket`, `region`

---

### Requirement: Local Development Override

Local development overrides SHALL use `application-local.yml` activated via `spring.profiles.active=local`.

`application-local.yml` SHALL be listed in `.gitignore` to prevent accidental commits of local credentials.

The local profile SHALL override only environment-specific values (DB credentials, Nacos addresses).

#### Scenario: Developer needs custom local database

- **WHEN** a developer has a local PostgreSQL on a non-standard port
- **THEN** they SHALL create `application-local.yml` with their custom `DB_HOST` and `DB_PORT`
- **AND** start the service with `--spring.profiles.active=local`
- **AND** the file SHALL NOT be committed to version control

---

### Requirement: Auto-Configuration Registration

Auto-configuration classes SHALL be registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

Each auto-configuration SHALL use `@ConditionalOnClass` or `@ConditionalOnProperty` to ensure it only activates when dependencies or configuration are present.

Custom enable annotations (e.g., `@EnableDruid`) MAY be provided for explicit feature activation.

#### Scenario: Registering a new auto-configuration

- **WHEN** adding `DruidAutoConfiguration` to `rinko-infra`
- **THEN** the class name SHALL be listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- **AND** the configuration SHALL use `@ConditionalOnClass(DruidDataSource.class)` to activate only when Druid is present

---

### Requirement: Git Configuration Management

All configuration files containing sensitive values (passwords, secrets, API keys) SHALL be excluded from Git via `.gitignore`.

The `.gitignore` SHALL exclude:
- `application-local.yml` — local development overrides with potential credentials
- `.env`, `.env.local` — environment variable files
- `*.pem`, `*.key`, `*.jks`, `*.p12` — cryptographic key material
- `*-credentials.json` — cloud service account credentials

Secrets required for CI/CD SHALL be stored in GitHub Secrets and injected as environment variables at build time, NEVER committed as files.

#### Scenario: Local development configuration stays local

- **WHEN** a developer creates `application-local.yml` with custom DB credentials
- **THEN** `git status` SHALL NOT show `application-local.yml` as an untracked file
- **AND** the credentials SHALL remain only on the developer's machine

#### Scenario: CI needs to access a container registry

- **WHEN** CI/CD pipeline pushes Docker images
- **THEN** registry credentials SHALL come from `secrets.GITHUB_TOKEN` or `secrets.GHCR_TOKEN`
- **AND** no credential file SHALL be present in the repository

---

### Requirement: JVM Runtime Configuration

The project SHALL include a `.mvn/jvm.config` file at the project root for Maven build JVM arguments.

`.mvn/jvm.config` SHALL contain `--add-opens` flags for JDK module exports and `-XX:+EnableDynamicAgentLoading` for Mockito agent support.

The `maven-surefire-plugin` in root `pom.xml` SHALL include equivalent JVM flags in its `argLine` configuration to ensure forked test JVMs inherit the same module access.

All service Dockerfiles SHALL include `--add-opens` flags in the `ENV JAVA_OPTS` for runtime module access.

#### Scenario: Maven build on JDK 21

- **WHEN** `./mvnw clean verify` is executed
- **THEN** no `java.lang.reflect.InaccessibleObjectException` warnings SHALL appear
- **AND** Spring, Jackson, and Mockito reflection SHALL work without errors

#### Scenario: Test execution without Mockito warnings

- **WHEN** `./mvnw test` runs with Mockito inline mock maker
- **THEN** the console SHALL NOT contain "Dynamic loading of agents will be disallowed"
- **AND** Mockito SHALL successfully create mocks

#### Scenario: Docker container starts on JDK 21

- **WHEN** a service Docker container starts
- **THEN** the JVM SHALL have `--add-opens` flags from `ENV JAVA_OPTS`
- **AND** the application SHALL start without module access errors
