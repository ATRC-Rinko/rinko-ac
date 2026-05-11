# CD Pipeline

## ADDED Requirements

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
