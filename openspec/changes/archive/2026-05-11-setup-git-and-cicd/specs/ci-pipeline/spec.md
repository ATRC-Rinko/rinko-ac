# CI Pipeline

## ADDED Requirements

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
