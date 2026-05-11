## Context

项目当前无版本控制、无 CI/CD。7 个子模块（1 个已实现 + 1 个骨架 infra + 5 个骨架业务模块）缺乏自动化质量门禁。项目使用 Maven 多模块构建（`mvnw`），已有 Checkstyle、SpotBugs、JaCoCo 插件配置在根 POM 中。基础设施由 `docker-compose.yml` 编排，但各服务模块没有 Dockerfile。

团队后续将进行多模块并行开发，Git 版本管理和 CI/CD 是协作前提。

## Goals / Non-Goals

**Goals:**
- 初始化 Git 仓库，配置完整的 Java/Kotlin/Maven/Docker 项目 `.gitignore`
- 搭建 GitHub Actions CI：PR/push 触发编译、测试、Checkstyle、SpotBugs、JaCoCo
- CI 中 JaCoCo 覆盖率不达标（line < 80% 或 branch < 70%）时构建失败
- 搭建 GitHub Actions CD：main 分支合并后按模块构建 Docker 镜像并推送
- 为每个业务模块创建标准化的多阶段 Dockerfile
- GitHub 社区健康文件：Issue 模板、PR 模板

**Non-Goals:**
- 不在 CI 中运行 TCL 集成测试（需要完整的 Docker Compose 环境，后续单独添加）
- 不配置 Kubernetes/Helm 部署
- 不配置 SonarQube（已有 SpotBugs + JaCoCo 覆盖静态分析 + 覆盖率）
- 不在本次配置 Git Hooks（预提交检查）

## Decisions

### 1. CI 触发策略：PR + push to main

**决策**: CI 在 `pull_request` 到 `main` 和 `push` 到 `main` 时触发。分支推送到非 main 分支不触发（由 PR 覆盖）。

**理由**: 避免重复运行。开发分支的 push 通过 PR 触发 CI，main 分支的 push 由合并后触发。

### 2. CD 触发策略：push to main + tag

**决策**: CD 在 `push to main`（latest 标签）和 `push tag v*`（版本标签）时触发。

**理由**: 分支 main 的每一次合并生成 latest 镜像用于开发环境，tag push 生成版本镜像用于生产部署。

### 3. Dockerfile 设计：多阶段构建 + SkyWalking Agent

**决策**: 使用 JDK 阶段 → Maven 阶段 → JRE 运行阶段的三阶段构建。
- Build 阶段：`eclipse-temurin:21-jdk-alpine` + Maven Wrapper
- Run 阶段：`eclipse-temurin:21-jre-alpine`
- SkyWalking Java Agent 通过构建参数可选集成（`skywalking-agent.jar`）

### 4. GitHub Actions 运行环境：ubuntu-latest

**决策**: 使用 GitHub-hosted `ubuntu-latest` runner。

**理由**: 免费、与 Docker 原生兼容、Maven 缓存支持良好。无需自托管 runner。

### 5. CI 使用 Maven Wrapper

**决策**: CI 中使用 `./mvnw` 而非系统 `mvn`。

**理由**: 确保所有环境和 CI 使用完全相同的 Maven 版本，避免版本差异导致的构建问题。

## Risks / Trade-offs

- **[风险] CI 中下载 Maven 依赖时间较长** → 使用 `actions/cache` 缓存 `~/.m2/repository`，通过 `pom.xml` 哈希控制缓存键
- **[风险] 多模块 Docker 构建 CI 时间过长** → CD 中使用矩阵策略只构建有变更的模块（通过 changed-files action）
- **[取舍] TCL E2E 测试暂不纳入 CI** → TCL 测试需要完整的 Docker Compose 环境（Nacos、PG、Redis、Kafka），在 GitHub Actions 中启动成本高，后续单独处理
