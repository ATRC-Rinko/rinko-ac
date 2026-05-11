# Coding Standards — Delta

## ADDED Requirements

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

The Maven configuration SHALL use `<pluginOptions>` to pass the annotation list to `kotlin-maven-plugin`:

```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <configuration>
        <pluginOptions>
            <option>all-open:annotation=org.springframework.stereotype.Component</option>
            <option>all-open:annotation=org.springframework.stereotype.Service</option>
            <option>all-open:annotation=org.springframework.stereotype.Repository</option>
            <option>all-open:annotation=org.springframework.transaction.annotation.Transactional</option>
            <option>all-open:annotation=org.springframework.context.annotation.Configuration</option>
            <option>all-open:annotation=org.springframework.web.bind.annotation.RestController</option>
        </pluginOptions>
    </configuration>
</plugin>
```

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
