# Coding Standards — Delta

## ADDED Requirements

### Requirement: Java Data Class Selection — Record vs Lombok

Java modules SHALL use **Java `record`** (JDK 14+) for immutable data transfer objects (DTOs) and **Lombok** for mutable Java Beans.

**Record** SHALL be used when:
- All fields are `final` (immutable after construction)
- No setters are needed
- Examples: Kafka message DTOs, query response DTOs, value objects

**Lombok `@Data`** SHALL be used when:
- Mutable state is required (JDBC/JPA entity mapping, configuration binding)
- Fields need setters for Spring Data or framework compatibility

**Lombok `@Getter` + `@Setter`** SHALL be used when:
- The class is a `@ConfigurationProperties` class where Spring injects values via setters
- Individual control over which fields are readable/writable is needed

Classes with non-trivial constructors (e.g., custom exception classes, Builder pattern classes) MAY keep explicit implementations.

The project SHALL include a `lombok.config` at the root with:
```
lombok.addLombokGeneratedAnnotation = true
```

Lombok SHALL be declared with `<scope>provided</scope>`.

#### Scenario: Creating a new immutable DTO in a Java module

- **WHEN** a developer creates a new DTO that carries data without mutation
- **THEN** the DTO SHALL be a Java `record`
- **AND** the class SHALL NOT be annotated with `@Data`
- **AND** no explicit constructor, getter, equals, hashCode, or toString SHALL be written

#### Scenario: Creating a new JDBC-mapped entity in a Java module

- **WHEN** a developer creates a new entity class mapped from JDBC `ResultSet`
- **THEN** the class SHALL use Lombok `@Data`
- **AND** it SHALL have a no-arg constructor and setters for framework compatibility

#### Scenario: Creating a configuration properties class

- **WHEN** a developer creates a `@ConfigurationProperties` class
- **THEN** the class SHALL use `@Getter` and `@Setter` at the class level
- **AND** Spring Boot relaxed binding SHALL work via the generated setters

#### Scenario: Lombok-generated code excluded from JaCoCo

- **WHEN** JaCoCo generates a coverage report
- **THEN** Lombok-generated methods SHALL NOT count toward coverage metrics
- **AND** only manually written code SHALL be measured
