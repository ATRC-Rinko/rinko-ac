# Configuration Standards — Delta

## ADDED Requirements

### Requirement: Git Configuration Management

All configuration files containing sensitive values (passwords, secrets, API keys) SHALL be excluded from Git via `.gitignore`.

The `.gitignore` SHALL exclude:
- `application-local.yml` — local development overrides with potential credentials
- `.env`, `.env.local` — environment variable files
- `*.pem`, `*.key`, `*.jks`, `*.p12` — cryptographic key material
- `*-credentials.json` — cloud service account credentials

Secrets required for CI/CD SHALL be stored in GitHub Secrets and injected as environment variables at build time, NEVER committed as files.

#### Scenario: Local development configuration stays local

- **WHEN** a developer creates `application-local.yml` with custom DB credentials
- **THEN** `git status` SHALL NOT show `application-local.yml` as an untracked file
- **AND** the credentials SHALL remain only on the developer's machine

#### Scenario: CI needs to access a container registry

- **WHEN** CI/CD pipeline pushes Docker images
- **THEN** registry credentials SHALL come from `secrets.GITHUB_TOKEN` or `secrets.GHCR_TOKEN`
- **AND** no credential file SHALL be present in the repository
