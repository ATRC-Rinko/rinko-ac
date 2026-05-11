# Testing Standards

This specification defines testing standards for the Rinko project. All requirements are derived from actual test patterns in `rinko-auth` and the testing strategy documented in `docs/spec.md`.

---

## ADDED Requirements

### Requirement: Unit Test Framework Selection

Java modules SHALL use JUnit 5 with Mockito for unit testing.

Kotlin modules SHALL use Kotest with Mockito for unit testing. MockK SHALL NOT be used due to Maven KMP metadata-only JAR incompatibility with JDK 21.

All modules SHALL include `spring-boot-starter-test` as a test dependency (inherited from root POM).

#### Scenario: Kotlin module test setup

- **WHEN** a developer writes a unit test in a Kotlin module
- **THEN** the test class SHALL extend Kotest's `StringSpec` or equivalent spec style
- **AND** Mockito SHALL be used for mocking (not MockK)
- **AND** the import SHALL be `org.mockito.kotlin.*` or `org.mockito.Mockito.*`

#### Scenario: Java module test setup

- **WHEN** a developer writes a unit test in a Java module
- **THEN** the test class SHALL use JUnit 5 annotations (`@Test`, `@BeforeEach`, `@ExtendWith`)
- **AND** Mockito SHALL be used via `@Mock` and `@InjectMocks` annotations or `MockitoExtension`

---

### Requirement: Code Coverage Requirements

All modules SHALL maintain minimum code coverage levels measured by JaCoCo:

| Metric | Minimum |
|--------|---------|
| Line Coverage | >= 80% |
| Branch Coverage | >= 70% |

JaCoCo plugin SHALL be configured in the root `pom.xml` with:
- `prepare-agent` goal bound to `initialize` phase
- `report` goal bound to `test` phase
- `check` goal bound to `verify` phase

Coverage verification SHALL fail the build when thresholds are not met.

#### Scenario: Running tests with coverage

- **WHEN** executing `mvn clean verify`
- **THEN** JaCoCo SHALL generate coverage reports at `{module}/target/site/jacoco/index.html`
- **AND** the build SHALL fail if line coverage < 80% or branch coverage < 70%

#### Scenario: Generating coverage report without verification

- **WHEN** executing `mvn clean test`
- **THEN** JaCoCo SHALL generate coverage reports
- **AND** the build SHALL NOT fail on coverage thresholds

---

### Requirement: Unit Test Structure

Each unit test SHALL test exactly one behavior. Test method names SHALL describe the scenario being tested.

Kotlin tests (Kotest StringSpec) SHALL use descriptive test names in natural language, enclosed in double quotes.

Java tests (JUnit 5) SHALL use `@DisplayName` for descriptive names.

#### Scenario: Testing wildcard permission matching

- **WHEN** testing the `WildcardMatcher`
- **THEN** each test SHALL cover exactly one match case (e.g., "should match exact permission code")
- **AND** each test SHALL cover exactly one non-match case (e.g., "should reject different segment count")

---

### Requirement: Mocking External Dependencies

Unit tests SHALL mock all external dependencies (databases, external services, message brokers). Unit tests SHALL NOT require a running PostgreSQL, Redis, RabbitMQ, or Kafka instance.

Controller tests SHALL mock service layer dependencies using `@WebFluxTest` or `@WebMvcTest` with `@MockBean`.

Repository tests SHALL use `@DataR2dbcTest` or `@DataJpaTest` with an in-memory database (H2).

#### Scenario: Testing a controller endpoint

- **WHEN** testing the AuthController login endpoint
- **THEN** the test SHALL use `@WebFluxTest(AuthController.class)` (Kotlin) or `@WebMvcTest(AuthController.class)` (Java)
- **AND** the service layer SHALL be mocked via `@MockBean`
- **AND** no actual database connection SHALL be required

#### Scenario: Testing a R2DBC repository

- **WHEN** testing a repository with custom @Query methods
- **THEN** the test SHALL use `@DataR2dbcTest` with H2 in-memory database
- **AND** the test SHALL verify SQL query correctness against real SQL execution

---

### Requirement: Test Data Management

Tests SHALL be independent and idempotent. Each test SHALL set up its own data and clean up afterward.

Tests SHALL NOT depend on the execution order of other tests.

Integration tests SHALL use a separate database instance (not the development or production database).

#### Scenario: Running the same test multiple times

- **WHEN** the same test is run 3 times consecutively
- **THEN** each run SHALL produce the same result
- **AND** no test data SHALL leak into subsequent runs

---

### Requirement: TCL Integration/E2E Test Requirements

TCL-based integration tests SHALL be placed in the `tests/` directory, organized by module (e.g., `tests/auth/`, `tests/oss/`).

TCL tests SHALL use `rl_json` for JSON data extraction and validation from HTTP responses.

Each TCL test file SHALL cover an end-to-end user scenario, covering gateway routing, authentication, and business logic in sequence.

TCL tests SHALL be run as a gating step in CI before deployment.

#### Scenario: Testing the full registration and login flow

- **WHEN** the `tests/auth/register_login.tcl` test is executed against a running system
- **THEN** it SHALL register a new user -> login -> access a protected resource
- **AND** all HTTP response assertions SHALL use `rl_json` for JSON value extraction

#### Scenario: CI pipeline gating

- **WHEN** the CI pipeline runs
- **THEN** all TCL tests SHALL pass before Docker images are built
- **AND** if any TCL test fails, the pipeline SHALL halt
