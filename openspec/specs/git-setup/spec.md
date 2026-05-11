# Git Setup

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
