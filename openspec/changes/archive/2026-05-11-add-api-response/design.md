## Context

项目有 5 个 Servlet 模块（Java，`rinko-log`/`rinko-oss`/`rinko-notify`/`rinko-scheduler` + `rinko-infra` 本身）和 2 个 WebFlux 模块（Kotlin，`rinko-gateway`/`rinko-auth`）。`rinko-infra` 作为共享库，为两类模块提供基础能力。

现有 `RinkoException` 体系（6 个异常类）和 `ProblemDetail`（RFC 7807）已在 infra 中定义，但缺少异常到 HTTP 响应的全局映射和统一成功响应包装。

## Goals / Non-Goals

**Goals:**
- `ApiResponse<T>` — 统一成功/错误响应格式：`{ code: 200, message: "OK", data: T, timestamp: "..." }`
- Servlet 全局异常处理器 — `@RestControllerAdvice` + `RinkoException` → `ApiResponse<?>`
- WebFlux 全局异常处理器 — Kotlin `@RestControllerAdvice` → `Mono<ResponseEntity<ApiResponse<?>>>`
- 自动注册：通过 `AutoConfiguration.imports` 使所有模块自动获得异常处理

**Non-Goals:**
- 不修改已有控制器代码（向后兼容）
- 不修改 `ProblemDetail` 类（保留 RFC 7807 支持）

## Decisions

### 1. 双实现：Java (Servlet) + Kotlin (WebFlux)

**决策**: rinko-infra 同时编译 Java 和 Kotlin。添加 `kotlin-stdlib` 依赖。

- `GlobalExceptionHandler.java` — Java，Spring MVC `@RestControllerAdvice`，处理 `RinkoException` 体系
- `ReactiveGlobalExceptionHandler.kt` — Kotlin，WebFlux `@RestControllerAdvice`，返回 `Mono<ResponseEntity<ApiResponse<?>>>`

Spring Boot 自动根据 classpath 是否存在 `spring-boot-starter-webflux` 或 `spring-boot-starter-web` 选择合适的处理器。两个 handler 都标记 `@ConditionalOnWebApplication` 以避免冲突。

实际上，`@RestControllerAdvice` 对两种类型都有效。关键在于：Spring WebFlux 应用不会加载 Spring MVC 的 `@RestControllerAdvice`。但为安全考虑，使用 `@ConditionalOnWebApplication(type = SERVLET)` 和 `@ConditionalOnWebApplication(type = REACTIVE)`。

### 2. ApiResponse 结构

```json
{
  "code": 200,
  "message": "OK",
  "data": { ... },
  "timestamp": "2026-05-11T10:00:00+08:00"
}
```

成功：`code = 200`，`data` 为业务数据。
错误：`code = 4xx/5xx`，`data = null`，`message` 为错误描述。

### 3. 自动注册

在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册两个 handler 类（作为 auto-configuration），确保所有模块的 `@SpringBootApplication` 扫描不到 `com.rinko.infra` 包时也能生效。

## Risks / Trade-offs

- **[风险] 两套 handler 在同一个 classpath 上可能冲突** → 使用 `@ConditionalOnWebApplication` 隔离
- **[取舍] ApiResponse 与 ProblemDetail 并存** → ProblemDetail 保留用于特殊场景（如 OAuth2 协议要求），ApiResponse 用于业务 API
