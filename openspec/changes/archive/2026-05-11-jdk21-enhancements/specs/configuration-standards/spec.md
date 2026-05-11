# Configuration Standards — Delta

## ADDED Requirements

### Requirement: JVM Runtime Configuration

The project SHALL include a `.mvn/jvm.config` file at the project root for Maven build JVM arguments.

`.mvn/jvm.config` SHALL contain:
```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.lang.reflect=ALL-UNNAMED
--add-opens java.base/java.lang.invoke=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
-XX:+EnableDynamicAgentLoading
```

These flags SHALL:
- Export JDK internal packages for Spring CGLIB proxy, Jackson serialization, and Mockito reflection
- Enable Mockito inline mock maker via ByteBuddy dynamic agent attach

All service Dockerfiles SHALL include the `--add-opens` flags in the `JAVA_OPTS` default:
```dockerfile
ENV JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
```

#### Scenario: Maven build on JDK 21

- **WHEN** `./mvnw clean verify` is executed
- **THEN** Maven Wrapper SHALL read `.mvn/jvm.config` and apply all JVM arguments
- **AND** Spring/Mockito/Jackson reflection access SHALL NOT produce `InaccessibleObjectException` warnings

#### Scenario: Running a Docker container with JDK 21

- **WHEN** a service Docker container starts
- **THEN** the JVM SHALL have `--add-opens` flags applied
- **AND** Spring Boot application SHALL start without module access warnings

#### Scenario: Test execution without Mockito agent warning

- **WHEN** `./mvnw test` runs with Mockito inline mock maker
- **THEN** the console output SHALL NOT contain "Dynamic loading of agents will be disallowed"
- **AND** Mockito SHALL successfully create mocks of final Kotlin classes
