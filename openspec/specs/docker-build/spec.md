# Docker Build

## ADDED Requirements

### Requirement: Multi-Stage Dockerfile for Every Service Module

Every deployable service module (all except `rinko-infra`) SHALL have a `Dockerfile` at the module root using multi-stage builds.

Stages SHALL be:
1. **JDK Stage** (`eclipse-temurin:21-jdk-alpine`) — copies Maven Wrapper and source, runs `./mvnw package -DskipTests -pl <module> -am`
2. **Runtime Stage** (`eclipse-temurin:21-jre-alpine`) — copies JAR from build stage, sets entrypoint

SkyWalking Java Agent SHALL be optionally integrated via a build ARG `SKYWALKING_AGENT` with default empty value.

#### Scenario: Building rinko-auth Docker image

- **WHEN** `docker build -t rinko-auth .` is run in `rinko-auth/`
- **THEN** the build SHALL compile rinko-auth and its dependencies (rinko-infra)
- **AND** the final image SHALL contain only the JAR and JRE
- **AND** the image SHALL be executable via `docker run rinko-auth`

#### Scenario: Building with SkyWalking Agent

- **WHEN** `docker build --build-arg SKYWALKING_AGENT=skywalking-agent.jar -t rinko-auth .` is run
- **THEN** the SkyWalking agent SHALL be copied into the runtime image
- **AND** the JVM entrypoint SHALL include `-javaagent:skywalking-agent.jar`

---

### Requirement: Docker Image Configuration

Docker images SHALL expose the module's assigned port via `EXPOSE`.

The entrypoint SHALL be:
```
ENTRYPOINT ["java", "-jar", "app.jar"]
```

JVM options SHALL be configurable via `JAVA_OPTS` environment variable.

#### Scenario: Running a container with custom JVM memory

- **WHEN** `docker run -e JAVA_OPTS="-Xmx512m" rinko-auth`
- **THEN** the JVM SHALL start with `-Xmx512m` heap limit
- **AND** the application SHALL listen on its configured port

---

### Requirement: Dockerfile Module Coverage

The following modules SHALL each have a `Dockerfile`:
- `rinko-gateway`
- `rinko-auth`
- `rinko-oss`
- `rinko-log`
- `rinko-notify`
- `rinko-scheduler`

`rinko-infra` SHALL NOT have a Dockerfile (it is a shared library, not a deployable service).

#### Scenario: Verifying all modules have Dockerfiles

- **WHEN** listing Dockerfiles in the project
- **THEN** 6 Dockerfiles SHALL exist (one per deployable module)
- **AND** `rinko-infra` SHALL NOT have a Dockerfile
