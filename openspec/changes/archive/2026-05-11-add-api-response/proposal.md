## Why

当前项目异常响应使用 RFC 7807 `ProblemDetail` 格式（`rinko-infra` 已有），但缺少统一的成功响应包装类 `ApiResponse<T>`。各模块控制器直接返回实体/DTO，响应格式不统一。同时，`RinkoException` 及其子类的异常需要在全局层面捕获并转换为标准响应格式，而当前没有全局异常处理器，每个模块各自处理（或不处理），导致响应格式不一致。

需要在 `rinko-infra` 中提供 `ApiResponse` 和全局异常处理器，区分 **Servlet**（Java）和 **WebFlux**（Kotlin）两套实现。

## What Changes

- `rinko-infra` 新增 `ApiResponse<T>` 通用响应类：`code`、`message`、`data`、`timestamp`
- `rinko-infra` 新增 `GlobalExceptionHandler`（Servlet）— `@RestControllerAdvice` 捕获 `RinkoException` 及其子类，转换为 `ApiResponse` 格式
- `rinko-infra` 新增 `ReactiveGlobalExceptionHandler`（WebFlux）— 同上，但返回 `Mono<ResponseEntity<ApiResponse<?>>>` 适配响应式
- 注册到 `AutoConfiguration.imports` 确保自动扫描生效
- 更新 `coding-standards` spec：所有控制器必须返回 `ApiResponse<T>`，异常统一由全局处理器处理

## Capabilities

### New Capabilities

<!-- 纯基础设施，无需新增业务 capability -->

### Modified Capabilities

- `coding-standards`: 新增统一响应规范 — 成功返回 `ApiResponse<T>`，异常由全局处理器自动转换

## Impact

- 新增文件：
  - `rinko-infra/.../dto/ApiResponse.java`
  - `rinko-infra/.../web/GlobalExceptionHandler.java` (Servlet)
  - `rinko-infra/.../web/ReactiveGlobalExceptionHandler.kt` (WebFlux, Kotlin)
- 修改文件：
  - `rinko-infra/.../META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` — 注册两个异常处理器
  - `openspec/specs/coding-standards/spec.md` — 新增 ApiResponse 规范
- 无依赖变更、无 API 破坏
