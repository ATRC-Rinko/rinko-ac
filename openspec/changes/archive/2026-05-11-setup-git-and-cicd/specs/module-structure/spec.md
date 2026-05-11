# Module Structure Standards — Delta

## ADDED Requirements

### Requirement: Dockerfile in Module Directory

Every deployable service module SHALL have a `Dockerfile` at the module root, alongside `pom.xml`.

The Dockerfile SHALL use multi-stage builds (JDK build stage → JRE runtime stage).

`rinko-infra` is a shared library and SHALL NOT have a Dockerfile.

The standard directory layout SHALL be updated to:
```
{module}/
├── pom.xml
├── Dockerfile          # Multi-stage build (service modules only)
├── src/
│   ├── main/
│   │   ├── java/ (or kotlin/)
│   │   └── resources/
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
