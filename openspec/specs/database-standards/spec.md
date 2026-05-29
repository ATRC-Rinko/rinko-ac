# Database Standards

This specification defines database standards for the Rinko project, covering table design conventions, entity and repository patterns, R2DBC/JDBC configuration, and SQL parameterization.

---

## ADDED Requirements

---

### Requirement: Table Design Conventions

All tables SHALL include:
- `id BIGINT PRIMARY KEY` — Snowflake-generated primary key
- `created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()` — creation timestamp
- `updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()` — last update timestamp

Table names SHALL use lowercase snake_case (e.g., `user_roles`, `role_permissions`).

Foreign key columns SHALL be named `{referenced_table_singular}_id` (e.g., `role_id` referencing `roles.id`).

Unique constraints SHALL be explicitly declared for natural keys (e.g., `UNIQUE(username)`, `UNIQUE(email)`).

#### Scenario: Creating a many-to-many join table

- **WHEN** creating the user-role association table
- **THEN** the table SHALL be named `user_roles`
- **AND** SHALL contain `user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`
- **AND** SHALL contain `role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE`
- **AND** SHALL have a composite primary key `PRIMARY KEY (user_id, role_id)`

#### Scenario: Creating a closure table for hierarchy

- **WHEN** implementing role hierarchy with closure table pattern
- **THEN** the table SHALL be named `role_hierarchy`
- **AND** SHALL include `ancestor_id`, `descendant_id`, and `depth` columns
- **AND** SHALL have `CHECK (depth >= 0 AND depth <= 3)` constraint

---

### Requirement: Entity Class Conventions

Entities SHALL be annotated with the appropriate Spring Data annotation:
- R2DBC (Kotlin/WebFlux): `@Table("table_name")` from `org.springframework.data.relational.core.mapping.Table`
- JDBC/JPA (Java/Servlet): `@Entity` + `@Table(name = "table_name")`

Entities SHALL be data classes (Kotlin) or POJOs with getters/setters (Java).

ID field SHALL be `Long` type, representing the Snowflake ID.

R2DBC entities SHALL NOT use JPA annotations (`@OneToMany`, `@ManyToMany`, `@JoinColumn`). Relationships SHALL be managed through explicit join queries in repositories.

#### Scenario: Creating an entity class in Kotlin (WebFlux)

- **WHEN** defining the User entity in `rinko-auth`
- **THEN** SHALL use `@Table("users")` (R2DBC)
- **AND** SHALL be a Kotlin data class with `val id: Long`
- **AND** SHALL NOT use JPA relationship annotations

#### Scenario: Creating an entity class in Java (Servlet)

- **WHEN** defining a FileMetadata entity in `rinko-oss`
- **THEN** SHALL use `@Entity` + `@Table(name = "files")` (JPA)
- **AND** SHALL have standard getters and setters
- **AND** SHALL have `@Id` on the id field

---

### Requirement: Repository Pattern

Repository interfaces SHALL extend the appropriate Spring Data base interface:
- R2DBC (WebFlux/Kotlin): `ReactiveCrudRepository<Entity, Long>`
- JPA (Servlet/Java): `JpaRepository<Entity, Long>`

Custom queries SHALL use `@Query` annotation with the appropriate SQL dialect:
- R2DBC: SQL with `:param` named parameters
- JPA: JPQL or native SQL with `nativeQuery = true`

Repository methods SHALL return:
- R2DBC: `Mono<Entity>` for single results, `Flux<Entity>` for multiple results
- JPA: `Optional<Entity>` for single results, `List<Entity>` for multiple results

#### Scenario: Finding a user by username in a reactive module

- **WHEN** defining `UserRepository`
- **THEN** SHALL extend `ReactiveCrudRepository<User, Long>`
- **AND** `findByUsername(String username)` SHALL return `Mono<User>`

#### Scenario: Custom join query for permissions by role ID

- **WHEN** defining `RoleRepository.findPermissionsByRoleId(Long roleId)`
- **THEN** SHALL use `@Query("SELECT p.* FROM permissions p JOIN role_permissions rp ON ... WHERE rp.role_id = :roleId")`
- **AND** SHALL return `Flux<Permission>` (R2DBC)

---

### Requirement: SQL Parameterization

All SQL queries SHALL use parameterized queries with named parameters (`:param`).

String concatenation for SQL construction SHALL NOT be used.

Dynamic queries (e.g., search filters) SHALL use programmatic query builders or criteria APIs.

#### Scenario: Building a dynamic LIKE query

- **WHEN** implementing wildcard permission matching with SQL LIKE
- **THEN** the LIKE pattern SHALL be constructed using SQL REPLACE functions: `REPLACE(:code, '*', '%')`
- **AND** SHALL NOT concatenate the pattern string in application code

---

### Requirement: Druid DataSource Configuration

Servlet modules with JDBC SHALL use Druid as the connection pool.

Druid SHALL be configured via `DruidDataSourceProperties` from `rinko-infra` with prefix `spring.datasource.druid`.

Default Druid settings SHALL be:
- `initialSize`: 5
- `maxActive`: 20
- `maxWait`: 60000ms
- StatFilter enabled for SQL monitoring and slow SQL detection (threshold: 1000ms)

The `@EnableDruid` annotation from `rinko-infra` SHALL be placed on the main application class or a configuration class.

#### Scenario: Configuring Druid for rinko-oss

- **WHEN** setting up `rinko-oss` database configuration
- **THEN** the main class or a config class SHALL be annotated with `@EnableDruid`
- **AND** Druid monitoring SHALL be accessible at `/druid/datasource.json`

---

### Requirement: R2DBC Configuration for WebFlux Modules

WebFlux modules (Kotlin) SHALL use R2DBC for reactive database access.

R2DBC connection factory SHALL be configured via `application.yml` properties:
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:rinko_auth}
    username: ${DB_USER:rinko}
    password: ${DB_PASSWORD:}
```

Modules SHALL use either R2DBC (for reactive/WebFlux modules) or JDBC (for servlet modules), not both simultaneously for runtime queries.

#### Scenario: rinko-auth database configuration

- **WHEN** `rinko-auth` starts up
- **THEN** the R2DBC connection SHALL be configured via `spring.r2dbc.*` properties
- **AND** runtime queries SHALL use R2DBC via `ReactiveCrudRepository`
