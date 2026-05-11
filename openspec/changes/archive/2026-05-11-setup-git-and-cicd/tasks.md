## 1. Git Repository Setup

- [x] 1.1 Create `.gitignore` file covering Java/Maven/Kotlin/IDE/Docker/OS files
- [x] 1.2 Create `.github/ISSUE_TEMPLATE/bug_report.md` and `.github/ISSUE_TEMPLATE/feature_request.md`
- [x] 1.3 Create `.github/PULL_REQUEST_TEMPLATE.md`
- [x] 1.4 Run `git init`, stage all files, and verify `git status` shows only intended files

## 2. CI Pipeline (GitHub Actions)

- [x] 2.1 Create `.github/workflows/ci.yml` — trigger on PR to main and push to main
- [x] 2.2 Configure JDK 21 setup, Maven Wrapper execution (`./mvnw clean verify`), and JaCoCo coverage gate
- [x] 2.3 Add Maven dependency caching via `actions/cache`

## 3. CD Pipeline (GitHub Actions)

- [x] 3.1 Create `.github/workflows/cd.yml` — trigger on push to main and tag `v*`
- [x] 3.2 Configure Docker Buildx, GHCR login via `GITHUB_TOKEN`, and multi-module image build and push

## 4. Dockerfiles for All Service Modules

- [x] 4.1 Create `rinko-gateway/Dockerfile` — multi-stage build
- [x] 4.2 Create `rinko-auth/Dockerfile` — multi-stage build with SkyWalking agent option
- [x] 4.3 Create `rinko-oss/Dockerfile` — multi-stage build
- [x] 4.4 Create `rinko-log/Dockerfile` — multi-stage build
- [x] 4.5 Create `rinko-notify/Dockerfile` — multi-stage build
- [x] 4.6 Create `rinko-scheduler/Dockerfile` — multi-stage build

## 5. Spec Sync

- [x] 5.1 Sync delta specs to main `openspec/specs/` — append new requirements to `configuration-standards` and `module-structure`
