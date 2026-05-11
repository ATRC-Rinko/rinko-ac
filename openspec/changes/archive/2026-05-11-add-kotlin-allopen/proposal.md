## Why

Kotlin 类和方法的默认修饰符是 `final`，而 Spring Boot 依赖 CGLIB 代理实现对 `@Configuration`、`@Service`、`@Repository`、`@Transactional` 等注解的增强。当前项目的 Kotlin 模块（`rinko-auth`、`rinko-gateway`）已在 **标注了常规 Spring 注解的类**（如 `AuthService`、`RoleController` 等），但缺少 `kotlin-allopen` 插件会导致以下问题：

1. 任何使用 `@Transactional` 的方法必须显式声明 `open`，否则事务静默失效
2. `@Configuration` 类无法被 CGLIB 代理，可能影响 Bean 的生命周期管理
3. 未来如需使用 Spring AOP（如切面日志、权限拦截），`final` 类将直接失败

现有代码目前尚未爆出运行时错误，是因为 Spring Boot 4.0 + Kotlin 2.1 在某些场景下对 `final` 类有更宽容的处理，但这种行为不可依赖。在生产环境中尽早配置 `allopen` 是 Kotlin + Spring 的标准实践。

## What Changes

- 根 `pom.xml` 添加 `kotlin-allopen` 依赖（与 kotlin 版本一致 2.1.0），并在 `kotlin-maven-plugin` 中注册为编译器插件
- `allopen` 注解列表配置：`@org.springframework.stereotype.Component`、`@org.springframework.stereotype.Service`、`@org.springframework.stereotype.Repository`、`@org.springframework.transaction.annotation.Transactional`、`@org.springframework.context.annotation.Configuration`、`@org.springframework.web.bind.annotation.RestController`
- 无需修改各子模块 `pom.xml`——当前 `rinko-auth` 和 `rinko-gateway` 已继承根 POM 的 `kotlin-maven-plugin` 配置
- 更新 `coding-standards` spec：补充 Kotlin allopen 编译器插件要求

## Capabilities

### New Capabilities

<!-- 本变更纯编译器配置，不需要新增业务 capability -->

### Modified Capabilities

- `coding-standards`: 补充 Kotlin allopen 编译器插件要求——所有 Kotlin 模块必须在 `kotlin-maven-plugin` 中配置 `kotlin-allopen`，注解列表至少包含 `@Component`、`@Service`、`@Repository`、`@Transactional`、`@Configuration`、`@RestController`

## Impact

- 影响文件：
  - `pom.xml`（根）：添加 `kotlin-allopen` 依赖版本属性和 plugin 配置
  - `openspec/specs/coding-standards/spec.md`：新增 allopen 需求项
- 受影响模块：`rinko-auth`、`rinko-gateway`（自动继承根 POM 配置，无需手动修改）
- 无 API 变更、无依赖冲突风险（allopen 仅影响编译期，运行时零开销）
