## Why

当前项目缺少版本控制系统和 CI/CD 流水线——没有 `.gitignore`、未初始化 Git 仓库、无自动化的构建/测试/部署流程。项目已进入多模块并行开发阶段（`rinko-auth` 已实现，`rinko-oss`/`rinko-log` 等骨架模块待开发），引入 Git 版本管理和 GitHub Actions CI/CD 是保障代码质量、支持多人协作、实现持续交付的必要基础。

## What Changes

- 初始化 Git 仓库，配置 `.gitignore`（覆盖 Java/Maven/Kotlin/Docker/IDE 文件）
- 创建 GitHub Actions CI 流水线：每个 PR / push 触发 `mvn clean verify`（编译、单元测试、Checkstyle、SpotBugs、JaCoCo 覆盖率检查）
- 创建 GitHub Actions CD 流水线：合并到 main 分支后构建 Docker 镜像并推送到 GitHub Container Registry
- 配置 JaCoCo 覆盖率门禁（line ≥ 80%, branch ≥ 70%），不达标时 CI 失败
- 添加 GitHub Issue 模板和 Pull Request 模板
- 为每个业务模块创建 Dockerfile（多阶段构建，基于 `eclipse-temurin:21-jre-alpine`）

## Capabilities

### New Capabilities

- `git-setup`: Git 仓库初始化、`.gitignore` 配置、分支策略
- `ci-pipeline`: GitHub Actions CI 流水线 — 编译、测试、静态分析、覆盖率检查
- `cd-pipeline`: GitHub Actions CD 流水线 — Docker 镜像构建与推送
- `docker-build`: 多模块 Dockerfile 规范 — 多阶段构建、SkyWalking Agent 集成

### Modified Capabilities

- `configuration-standards`: 补充 Git 配置管理规范（`.gitignore`、敏感信息管理）
- `module-structure`: 补充 Dockerfile 在模块标准目录结构中的位置

## Impact

- 新增文件：
  - `.gitignore` — Git 忽略规则
  - `.github/workflows/ci.yml` — CI 流水线
  - `.github/workflows/cd.yml` — CD 流水线
  - `.github/ISSUE_TEMPLATE/bug_report.md` — Bug 报告模板
  - `.github/ISSUE_TEMPLATE/feature_request.md` — 功能请求模板
  - `.github/PULL_REQUEST_TEMPLATE.md` — PR 模板
  - 每个业务模块 `Dockerfile`（7 个）
- 现有文件影响：无代码变更
- 外部依赖：GitHub Actions、GitHub Container Registry
