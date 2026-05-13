## Why

项目中存在 18 处直接抛出 JDK 异常（`RuntimeException`、`IllegalArgumentException`、`IllegalStateException`）的业务代码，这些异常不会被 `GlobalExceptionHandler` 统一捕获为 `RinkoException` 子类，导致 API 返回非标准的错误格式（例如 `RuntimeException` 包装的 `IOException` 在 `LocalStorageService` 中返回裸 500 而非结构化 `ApiResponse.error(...)`）。全部替换为 `RinkoException` 子类可确保全局异常处理器正确格式化所有错误响应。

## What Changes

- 16 处 `throw new RuntimeException(...)` → `throw new InternalException(...)`（系统内部错误，映射 HTTP 500）
- 1 处 `throw new RuntimeException(...)` → `throw new NotFoundException(...)`（`LocalStorageService.getInputStream` 文件不存在 = HTTP 404）
- 1 处 `throw new IllegalArgumentException(...)` → `throw new ValidationException(...)`（`SortOrder.fromString` 排序方向非法 = HTTP 400）
- 1 处 `throw new IllegalStateException(...)` → `throw new InternalException(...)`（`SnowflakeIdGenerator` 时钟回拨 = HTTP 500）
- 移除不需要的 JDK 异常 import，添加 RinkoException 子类 import

## Capabilities

### New Capabilities

None. 纯实现变更，不新增 API 端点或行为变更。

### Modified Capabilities

None. 异常类型替换后错误响应的 HTTP 状态码和 errorCode 保持不变（均由 GlobalExceptionHandler 映射）。

## Impact

**涉及 8 个文件，分布在 4 个模块：**

| 模块 | 文件 | 替换数 |
|------|------|--------|
| rinko-infra | `SortOrder.java` | 1 (IllegalArgumentException → ValidationException) |
| rinko-infra | `SnowflakeIdGenerator.java` | 3 (2×IllegalArgumentException + 1×IllegalStateException → InternalException) |
| rinko-oss | `LocalStorageService.java` | 6 (5×RuntimeException → InternalException + 1×NotFoundException) |
| rinko-oss | `FileService.java` | 1 (RuntimeException → InternalException) |
| rinko-scheduler | `SchedulerService.java` | 4 (RuntimeException → InternalException) |
| rinko-scheduler | `ShellJobExecutor.java` | 1 (RuntimeException → InternalException) |
| rinko-scheduler | `BeanJobExecutor.java` | 1 (RuntimeException → InternalException) |
| rinko-scheduler | `HttpJobExecutor.java` | 1 (RuntimeException → InternalException) |
