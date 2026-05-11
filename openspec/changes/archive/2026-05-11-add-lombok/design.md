## Context

项目有 5 个 Java 模块（`rinko-infra`、`rinko-log` 已实现，`rinko-oss`/`rinko-notify`/`rinko-scheduler` 骨架）。所有 Java 实体、DTO、配置类使用手写 getter/setter，代码行数中约 40% 是 getter/setter 样板代码。Lombok 是 Java 生态标准工具，通过编译期注解处理器自动生成 `getXxx()`/`setXxx()`/`toString()`/`equals()`/`hashCode()` 等。

当前 JDK 21 + Maven 3.9 完全兼容 Lombok 最新版本。

## Goals / Non-Goals

**Goals:**
- 添加 Lombok 依赖（`provided` scope，编译期生成，运行时无依赖）
- 在 `coding-standards` spec 中增加 Lombok 使用规范
- 为现有 Java 实体/DTO/配置类添加 Lombok 注解

**Non-Goals:**
- 不强制将已有代码全部重构为 Lombok（`RinkoException` 等构造逻辑在构造函数中的类保持现有风格）
- Kotlin 模块不受影响（已有 data class）
- 不使用 Lombok 的 `@Builder`、`@AllArgsConstructor` 等高级特性（后续按需引入）

## Decisions

### 1. Lombok scope: `provided`

**决策**: Lombok `<scope>provided</scope>`。

**理由**: Lombok 仅在编译期需要，运行时不需要。`provided` 确保最终 JAR 中不包含 Lombok JAR。

### 2. 数据类型选择：Record vs Lombok @Data

**决策**: 不可变 DTO 使用 Java `record`，可变 Bean 使用 Lombok。

| 类型 | 工具 | 适用场景 |
|------|------|----------|
| `record` | JDK 原生 | 不可变 DTO：所有字段 `final`，构造后不修改 |
| `@Data` | Lombok | 可变实体：JPA/JDBC 映射、需要无参构造 + setter |
| `@Getter @Setter` | Lombok | 配置类：`@ConfigurationProperties` 需要 setter 注入 |

**当前类分类**:
- `record`：`LogMessage`（Kafka 消息）、`PageResponse`（所有 final 字段）
- `@Data`：`LogEntry`（JDBC 映射）、`LogLevelConfig`（Spring Data 实体）
- `@Getter @Setter`：`LogProperties`、`CorsProperties`、`DruidDataSourceProperties`
- 保持不变：`RinkoException`（自定义构造函数）、`ProblemDetail`（Builder 模式）、`PageRequest`（校验逻辑）

**理由**: `record` 比 Lombok `@Data` 更优——无需额外依赖、编译期强制不可变、`equals`/`hashCode`/`toString` 自动生成且语义正确。但 `record` 没有 setter，不适合需要反序列化注入的配置类和 JDBC 映射的实体。

### 3. 版本管理：根 POM dependencyManagement

**决策**: Lombok 版本在根 POM 的 `<properties>` 中声明为 `lombok.version`，通过 `<dependencyManagement>` 统一管理。子模块只需引入不写版本。

## Risks / Trade-offs

- **[风险] Lombok 注解处理器与 IDE 不兼容** → 现代 IDE (IntelliJ IDEA 2024+, Eclipse) 均已内置 Lombok 插件支持，本地开发需安装 Lombok 插件
- **[风险] JaCoCo 可能将 Lombok 生成的代码计入覆盖率** → 在根目录添加 `lombok.config`：`lombok.addLombokGeneratedAnnotation = true`，JaCoCo 自动跳过 `@Generated` 标记的代码
