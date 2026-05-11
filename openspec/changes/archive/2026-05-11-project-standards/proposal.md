## Why

基于现有 `docs/spec.md`（v1.2）架构文档和已实现的 `rinko-infra`、`rinko-auth` 代码，梳理并生成可落地的、能直接指导后续模块开发的项目规范体系。当前项目有宪法（constitution.md）定义不可变原则，有 spec.md 定义架构，但缺少面向开发者的**编码规范、测试规范、模块结构规范、API 设计规范**等实操层指南，导致骨架模块（rinko-oss/log/notify/scheduler/gateway）在实现时缺乏统一的标准参照。

## What Changes

- 新增 `coding-standards` 规范：定义 Java/Kotlin 代码风格、命名约定、包结构、注释规范、异常处理模式
- 新增 `testing-standards` 规范：定义 JUnit 5 + Kotest + Mockito 测试编写标准、覆盖率基准、TCL E2E 测试规范
- 新增 `module-structure` 规范：定义各模块的标准目录结构、配置文件、依赖声明模式
- 新增 `api-design` 规范：定义 RESTful API 设计标准、RFC 7807 错误响应格式、OpenAPI 3.0 文档要求
- 新增 `database-standards` 规范：定义 Flyway 迁移脚本规范、实体类规范、R2DBC/JDBC 使用模式
- 新增 `configuration-standards` 规范：定义 Nacos 配置管理规范、application.yml 结构、环境变量约定
- 以上规范均基于现有代码中的**实际实现模式**提取，确保规范与已有代码一致，而非凭空臆造

## Capabilities

### New Capabilities

- `coding-standards`: Java/Kotlin 编码规范 — 代码风格、包结构、命名约定、注释文档、异常处理、日志规范
- `testing-standards`: 测试规范 — 单元测试（JUnit 5 / Kotest）、Mock 规范（Mockito）、覆盖率基准、TCL E2E 测试编写标准
- `module-structure`: 模块结构规范 — 标准目录布局、pom.xml 配置模板、WebFlux vs Servlet 模块差异
- `api-design`: API 设计规范 — RESTful 路径、版本化、RFC 7807 错误格式、OpenAPI 3.0 注解规范
- `database-standards`: 数据库规范 — Flyway migration 脚本格式、实体类注解、R2DBC Repository 模式、表设计约定
- `configuration-standards`: 配置管理规范 — Nacos 配置结构、application.yml 模板、环境变量命名与注入

### Modified Capabilities

<!-- 本次为全新创建，不修改已有 capability -->

## Impact

- 影响范围：所有已有和新增模块
  - `rinko-infra`、`rinko-auth` 已实现代码作为规范的**来源参照**（不修改，只提取模式）
  - `rinko-gateway`、`rinko-oss`、`rinko-log`、`rinko-notify`、`rinko-scheduler` 骨架模块将**遵循新规范**进行实现
- 文档结构：新增 6 个 spec 文件（`openspec/specs/{name}/spec.md`），不影响现有 `docs/` 目录文档
- 无 API 变更、无依赖变更、无基础设施变更
