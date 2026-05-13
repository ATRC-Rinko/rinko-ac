## Context

项目自定义异常体系位于 `rinko-infra` 模块的 `com.rinko.infra.exception` 包：

```
RinkoException (abstract, extends RuntimeException)
├── ValidationException     → HTTP 400, errorCode="VALIDATION_ERROR"
├── UnauthorizedException   → HTTP 401, errorCode="UNAUTHORIZED"
├── ForbiddenException      → HTTP 403, errorCode="FORBIDDEN"
├── NotFoundException       → HTTP 404, errorCode="NOT_FOUND"
└── InternalException       → HTTP 500, errorCode="INTERNAL_ERROR"
```

两个全局异常处理器 (`GlobalExceptionHandler` 和 `ReactiveGlobalExceptionHandler`) 已注册所有 5 个子类的 `@ExceptionHandler`，并有一个 `@ExceptionHandler(Exception.class)` 兜底。当前 18 处 JDK 异常将被替换为对应子类，确保所有业务错误走统一处理路径。

## Goals / Non-Goals

**Goals:**
- 所有业务代码中抛出的 JDK 异常替换为 `RinkoException` 子类
- 保持现有 HTTP 状态码和 errorCode 语义不变（400/404/500）
- 保留原始 `cause` 异常（如 `IOException`、`SchedulerException`）作为 `RinkoException` 的 `cause`

**Non-Goals:**
- 不新增异常子类（现有 5 个已覆盖全部需求）
- 不修改 `GlobalExceptionHandler`
- 不修改 Controller 层（已是使用 `RinkoException` 子类）

## Decisions

1. **映射规则**:
   - `RuntimeException` 包装 IO/Quartz/反射异常 → `InternalException`（系统内部错误）
   - `IllegalArgumentException` 用于用户输入校验 → `ValidationException`（参数校验失败）
   - `IllegalStateException` 用于系统状态异常 → `InternalException`（内部状态错误）
   - 文件不存在场景 → `NotFoundException`（资源未找到）

2. **`SnowflakeIdGenerator` 构造器参数校验保留 `IllegalArgumentException`**: 该类的 `workerId`/`datacenterId` 校验只在手动传参构造时触发（默认无参构造自动计算合法值），属于编程错误而非用户输入错误，保留原样。

3. **保留原始异常作为 cause**: 所有替换后的 `InternalException` 和 `NotFoundException` 通过双参构造器 `new InternalException(message, originalException)` 保留调用栈。

## Risks / Trade-offs

- **Risk: 错误响应的 HTTP 状态码变化** — 之前 `RuntimeException` 走兜底 handler 返回 500，替换为 `NotFoundException` 会变为 404（仅 `LocalStorageService.getInputStream` 一例）。**Mitigation: 这是有意的修正 — 文件不存在本就应返回 404。**
