## Context

Kotlin 编译器默认将所有类和方法的访问修饰符设为 `final`。Spring Boot 通过 CGLIB 代理实现 AOP（事务、缓存、安全、配置增强），要求被代理的类和方法是 `open` 的。

当前项目 `rinko-auth` 和 `rinko-gateway` 两个 Kotlin 模块已使用 `kotlin-maven-plugin` 编译，但未配置 `kotlin-allopen` 编译器插件。现阶段未出问题仅因当前代码尚未触发需要 CGLIB 代理的场景（如 `@Transactional` 方法、AOP 切面）。

Spring Initializr 对 Kotlin + Spring Boot 项目的默认配置即包含 `kotlin-allopen`，这是 Kotlin + Spring 生态的标准实践。

## Goals / Non-Goals

**Goals:**
- 在根 POM 的 `kotlin-maven-plugin` 中注册 `kotlin-allopen` 编译器插件
- 覆盖 Spring 最常用的 6 个注解：`@Component`、`@Service`、`@Repository`、`@Configuration`、`@Transactional`、`@RestController`
- 所有 Kotlin 模块自动继承该配置，无需子模块修改
- 在 `coding-standards` spec 中记录该要求

**Non-Goals:**
- 不需要 `kotlin-noarg` 插件（JPA 实体无参构造函数生成，本项目使用 R2DBC，不需要）
- 不需要 `kotlin-jpa` 插件
- 不引入新的 Maven 依赖或 BOM
- 不修改已有代码的业务逻辑

## Decisions

### 1. 在根 POM 的 pluginManagement 中配置 allopen

**决策**: 在 `<pluginManagement>` 的 `kotlin-maven-plugin` 配置中添加 `<pluginOptions>`，而非在各模块 POM 中分别配置。

**理由**: 项目只有 2 个 Kotlin 模块，且都继承根 POM 的 plugin 配置。集中配置避免重复，且确保未来新增 Kotlin 模块自动获得 allopen 支持。

### 2. 注解选择：覆盖 Spring 常规注解体系

**决策**: allopen 注解列表：
```
org.springframework.stereotype.Component
org.springframework.stereotype.Service
org.springframework.stereotype.Repository
org.springframework.transaction.annotation.Transactional
org.springframework.context.annotation.Configuration
org.springframework.web.bind.annotation.RestController
```

**理由**:
- `@Component`/`@Service`/`@Repository`: 覆盖所有 Spring Bean，`@Component` 是 `@Service`/`@Repository` 的元注解
- `@Configuration`: Spring 对 `@Configuration` 类必须代理以保证单例语义
- `@Transactional`: 事务代理的必需
- `@RestController`: 覆盖所有控制器（含 `@Controller` — 但因为 `@RestController` 已含 `@Controller` 语义，只需声明 `@RestController`）

### 3. 不引入 `kotlin-spring` 包装插件

**决策**: 直接使用 `kotlin-allopen` 基础插件并显式列出注解，而非使用 `kotlin-spring` 包装插件。

**理由**: `kotlin-spring` wrapper 自动包含了 Spring 所有常见注解的 `allopen` 配置，但引入额外 JAR 依赖。显式列出注解的方式更透明，且无需额外依赖版本管理。

## Risks / Trade-offs

- **[风险] 注解列表不完整，遗漏未来需要的注解** → 可通过后续 PR 扩充注解列表，属低风险
- **[取舍] 不使用 `kotlin-spring` wrapper，需手动维护注解列表** → 接受此 trade-off，显式配置更可控，避免被 wrapper 版本变化影响
- **[风险] allopen 不会影响已有编译缓存** → 首次添加后需执行 `mvn clean compile` 重新编译
