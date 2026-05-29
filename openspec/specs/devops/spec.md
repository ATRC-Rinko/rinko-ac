# DevOps

## ADDED Requirements

### Requirement: Git Repository Initialization

The project SHALL be version-controlled with Git. The repository SHALL be hosted on GitHub under the owner/name determined at setup time.

The `.gitignore` file SHALL exclude at minimum:
- Build output: `target/`, `*.class`, `*.jar`
- Maven: `.mvn/wrapper/maven-wrapper.jar`
- IDE files: `.idea/`, `.vscode/`, `*.iml`, `*.iws`, `*.ipr`
- OS files: `.DS_Store`, `Thumbs.db`
- Environment: `.env`, `.env.local`, `application-local.yml`
- Logs: `logs/`, `*.log`
- Docker: `_build/`
- Node (if applicable): `node_modules/`

#### Scenario: First-time git initialization from scratch

- **WHEN** `git init` is run and `.gitignore` is committed
- **THEN** `git status` SHALL NOT show `target/`, `.idea/`, or `.env` files as untracked
- **AND** `git add .` SHALL only stage source code and configuration files

#### Scenario: Developer opens project in IntelliJ IDEA

- **WHEN** IntelliJ generates `.idea/` project files
- **THEN** `git status` SHALL NOT show `.idea/` as untracked changes

---

### Requirement: Branch Strategy

The repository SHALL use a trunk-based development model:
- `main` — production-ready code, protected from direct push
- Feature branches — `feature/<description>` or `<developer>/<description>`
- Bug fix branches — `fix/<description>`

All changes to `main` SHALL go through a Pull Request with at least one approval.

#### Scenario: Developer starts a new feature

- **WHEN** a developer starts work on a new feature "add file upload"
- **THEN** they SHALL create a branch `feature/add-file-upload` from `main`
- **AND** changes SHALL be merged back to `main` via a Pull Request

#### Scenario: Direct push to main is blocked

- **WHEN** a developer attempts to push directly to `main`
- **THEN** the push SHALL be rejected (branch protection enabled on GitHub)

---

### Requirement: Commit Message Convention

Commit messages SHALL follow the Conventional Commits format:
```
<type>(<scope>): <description>

[optional body]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `ci`, `build`

Scope SHALL be the module name (e.g., `auth`, `infra`, `gateway`, `oss`) or `root` for project-level changes.

#### Scenario: Committing a new feature for rinko-auth

- **WHEN** a developer commits a new JWT refresh endpoint in rinko-auth
- **THEN** the commit message SHALL be `feat(auth): add token refresh endpoint`

#### Scenario: Commit for CI configuration change

- **WHEN** a developer updates the GitHub Actions workflow
- **THEN** the commit message SHALL be `ci: update CI workflow to include SpotBugs check`

---

### Requirement: Sensitive Information Exclusion

Sensitive information SHALL NOT be committed to the repository.

The `.gitignore` SHALL exclude files containing credentials:
- `application-local.yml` (local development overrides)
- `.env`, `.env.local` (environment variables)
- `*.pem`, `*.key`, `*.jks` (certificate/keystore files)
- `*-credentials.json` (cloud credentials)

Secrets required for CI/CD SHALL be stored in GitHub Secrets, not in repository files.

#### Scenario: A developer accidentally adds a file with database password

- **WHEN** `application-local.yml` containing `DB_PASSWORD` is staged
- **THEN** `git status` SHALL NOT show it (excluded by `.gitignore`)
- **AND** the file SHALL remain only on the developer's machine

#### Scenario: CI needs Docker registry credentials

- **WHEN** CI pipeline pushes Docker images
- **THEN** registry credentials SHALL be injected from GitHub Secrets (`GHCR_USERNAME`, `GHCR_TOKEN`)
- **AND** no credential files SHALL exist in the repository

---

### Requirement: Build and Test on Pull Request

The CI pipeline SHALL trigger on every pull request to `main` and on every push to `main`.

The pipeline SHALL run the following steps in order:
1. Checkout code
2. Set up JDK 21 (Temurin distribution)
3. Cache Maven local repository (`~/.m2/repository`)
4. Run `./mvnw clean verify` (compile, test, Checkstyle, SpotBugs, JaCoCo)

The pipeline SHALL fail if any step fails.

#### Scenario: Developer opens a pull request

- **WHEN** a PR is opened against `main`
- **THEN** GitHub Actions SHALL automatically trigger the CI workflow
- **AND** PR status SHALL show a check named "CI / Build and Test"
- **AND** the check SHALL pass if `mvn clean verify` succeeds

#### Scenario: Test failure blocks merge

- **WHEN** a PR has failing tests
- **THEN** the CI check SHALL fail
- **AND** the PR SHALL show a red X status
- **AND** GitHub branch protection SHALL prevent merging

---

### Requirement: Code Quality Checks

The CI pipeline SHALL execute static analysis tools configured in the project:
- Checkstyle: verify code style compliance against `checkstyle.xml`
- SpotBugs: detect potential bugs via bytecode analysis

Both checks SHALL be part of the `mvn verify` lifecycle.

At minimum, the CI SHALL report warnings in the build log. The pipeline SHALL fail if Checkstyle or SpotBugs detect violations.

#### Scenario: Code style violation in PR

- **WHEN** a PR contains code that violates `checkstyle.xml` rules
- **THEN** the CI build SHALL fail with Checkstyle violation details
- **AND** the developer SHALL fix violations before merging

---

### Requirement: JaCoCo Coverage Gate

The CI pipeline SHALL generate JaCoCo coverage reports during the `verify` phase.

Coverage thresholds SHALL be:
- Line coverage >= 80%
- Branch coverage >= 70%

If any module falls below these thresholds, the build SHALL fail.

#### Scenario: New module has insufficient test coverage

- **WHEN** a module has line coverage below 80%
- **THEN** `mvn verify` SHALL fail
- **AND** the failure message SHALL indicate which module and metric failed
- **AND** the PR SHALL be blocked from merging

#### Scenario: Existing module maintains coverage above threshold

- **WHEN** all modules meet coverage thresholds
- **THEN** `mvn verify` SHALL pass the JaCoCo check
- **AND** coverage reports SHALL be available as build artifacts

---

### Requirement: Maven Dependency Caching

The CI pipeline SHALL cache Maven dependencies to reduce build time.

The cache key SHALL be based on the hash of all `pom.xml` files in the project.

The cache SHALL be restored before `mvn verify` and saved after.

#### Scenario: CI runs on consecutive commits

- **WHEN** the second CI run starts after a cache hit
- **THEN** Maven dependency download time SHALL be significantly reduced
- **AND** only changed dependencies SHALL be downloaded

---

### Requirement: Docker Image Build and Push on Main Branch

The CD pipeline SHALL trigger on every push to `main` (after CI passes).

The pipeline SHALL for each service module:
1. Build the module using Maven Wrapper
2. Build a Docker image using the module's `Dockerfile`
3. Push the image to GitHub Container Registry (GHCR) with tag `latest`

The image naming convention SHALL be: `ghcr.io/<owner>/<module>:latest`

#### Scenario: Merge to main triggers CD

- **WHEN** a PR is merged to `main`
- **THEN** the CD workflow SHALL trigger automatically
- **AND** Docker images for all changed modules SHALL be built and pushed to GHCR
- **AND** each image SHALL be tagged with `latest`

---

### Requirement: Versioned Release on Git Tag

When a Git tag matching `v*` (e.g., `v1.0.0`) is pushed, the CD pipeline SHALL:

1. Build all service modules
2. Build Docker images tagged with the version from the tag (e.g., `v1.0.0`)
3. Push versioned images to GHCR

The `latest` tag SHALL NOT be updated on versioned releases (reserved for main branch).

#### Scenario: Creating a release

- **WHEN** a maintainer pushes a tag `v1.0.0`
- **THEN** the CD workflow SHALL build Docker images tagged `v1.0.0`
- **AND** push them to `ghcr.io/<owner>/<module>:v1.0.0`

---

### Requirement: GitHub Container Registry Authentication

The CD pipeline SHALL authenticate to GHCR using the `GITHUB_TOKEN` provided by GitHub Actions.

GHCR image names SHALL follow the pattern `ghcr.io/<repository-owner>/<module>:<tag>`.

#### Scenario: Pipeline pushes Docker image

- **WHEN** CD workflow runs
- **THEN** `docker login ghcr.io` SHALL authenticate using `GITHUB_TOKEN`
- **AND** images SHALL be pushed to the repository's GHCR namespace

---

### Requirement: Module Selection Strategy

The CD pipeline SHALL only build and push Docker images for modules that have source code changes since the last build, OR all modules on tag-triggered releases.

Changed-module detection SHALL use a diff-based approach comparing the current commit with the base.

#### Scenario: Only rinko-auth source changed

- **WHEN** only files under `rinko-auth/` are modified in a merge to main
- **THEN** the CD pipeline SHALL only build and push `rinko-auth` Docker image
- **AND** other modules SHALL be skipped

#### Scenario: Version tag release

- **WHEN** tag `v1.0.0` is pushed
- **THEN** CD SHALL build and push Docker images for ALL service modules
- **AND** each image SHALL be tagged with `v1.0.0`

---

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
