## Why

项目的 Java 模块中大量实体类和 DTO 需要手写 `getXxx()`/`setXxx()` 方法，代码冗长。JDK 21 原生支持 `record`（不可变数据载体）和 Lombok（可变 Bean）两种互补方案，应根据类的语义选择：`record` 用于不可变 DTO，Lombok 用于需要 setter 的可变 Java Bean。Kotlin 模块已有 `data class`，本次仅覆盖 Java 模块。

## What Changes

- 根 `pom.xml` 添加 Lombok 依赖（`provided` scope，编译期生效）
- 创建 `lombok.config`，配置 `lombok.addLombokGeneratedAnnotation = true`（JaCoCo 自动跳过生成代码）
- 将不可变 DTO 重构为 Java `record`：`LogMessage`、`PageResponse` — 删除所有手写 constructor/getter/equals/hashCode
- 为可变 Java Bean 添加 Lombok 注解：`LogEntry`、`LogLevelConfig` → `@Data`；`LogProperties`、`CorsProperties`、`DruidDataSourceProperties` → `@Getter` + `@Setter`
- 有自定义构造逻辑的类保持不变：`RinkoException`、`ProblemDetail`、`PageRequest`、`SortOrder`
- 更新 `coding-standards` spec：Java 数据类二选一规则 —— 不可变用 `record`，可变用 Lombok

## Capabilities

### New Capabilities

<!-- 纯基础设施变更，无需新增业务 capability -->

### Modified Capabilities

- `coding-standards`: 新增 "Java Record vs Lombok" 需求 — 不可变 DTO 用 `record`、可变 Bean 用 Lombok `@Data` / `@Getter`+`@Setter`

## Impact

- 依赖新增：`org.projectlombok:lombok` (provided scope)
- 新增文件：`lombok.config`（根目录）
- 影响文件：
  - `pom.xml`（根）：添加 Lombok 版本管理 + 全局依赖
  - `rinko-log/dto/LogMessage.java`：class → record
  - `rinko-infra/dto/PageResponse.java`：class → record
  - `rinko-log/entity/LogEntry.java`、`LogLevelConfig.java`：添加 `@Data`
  - `rinko-log/config/LogProperties.java`、`rinko-infra/.../CorsProperties.java`、`DruidDataSourceProperties.java`：添加 `@Getter @Setter`
  - `openspec/specs/coding-standards/spec.md`：新增 record + Lombok 规范
- Kotlin 模块不受影响
