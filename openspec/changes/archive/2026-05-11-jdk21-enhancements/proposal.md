## Why

项目使用 JDK 21 编译和运行，但未启用任何 JDK 21 新增特性：无 JPMS 模块导出配置（运行时报 `java.lang.reflect.InaccessibleObjectException` 警告）、未启用 Virtual Threads（Servlet 模块仍在用平台线程池）、Mockito 每次测试都触发 "Dynamic loading of agents" 警告。JDK 21 的这些特性已正式发布（非 Preview），项目应及时适配以消除警告、提升性能、降低运维成本。

## What Changes

- 创建 `.mvn/jvm.config`：统一 Maven 构建阶段的 JVM 参数——`--add-opens` 导出核心模块供 Spring/Mockito/Jackson 反射访问，配置 Mockito agent
- 在 Nacos 公共配置 `application-dev.yml` 中启用 Virtual Threads：`spring.threads.virtual.enabled=true`，Servlet 模块（Jetty）自动使用虚拟线程处理请求
- 更新所有 6 个服务的 `Dockerfile`：添加 `--add-opens` JVM 运行时参数
- 在 `docker-compose.yml` 等服务配置中添加 JDK 21 运行时参数
- 更新 `coding-standards` spec：新增 JDK 21 特性使用指南（Record 选择标准、Virtual Threads 适用场景、JPMS 模块导出规范）
- 为 Servlet 模块 `rinko-log` 添加 `@EnableVirtualThreads` (Spring Boot 4.0 风格，实际通过配置启用)

## Capabilities

### New Capabilities

<!-- 纯基础设施配置，无需新增业务 capability -->

### Modified Capabilities

- `coding-standards`: 新增 "JDK 21 Feature Usage" 需求——Virtual Threads 适用 Servlet 模块、Record 用于不可变 DTO、JPMS `--add-opens` 构建/运行配置
- `configuration-standards`: 新增 JVM 配置规范——`.mvn/jvm.config` 构建参数、Docker 运行时 JVM 参数

## Impact

- 新增文件：`.mvn/jvm.config`
- 修改文件：
  - `nacos-config/application-dev.yml` — 添加 `spring.threads.virtual.enabled=true`
  - 所有 6 个 `Dockerfile` — 添加 `--add-opens` JVM 参数
  - `openspec/specs/coding-standards/spec.md` — 新增 JDK 21 特性需求
  - `openspec/specs/configuration-standards/spec.md` — 新增 JVM 配置需求
- 性能影响：Servlet 模块请求并发能力显著提升（虚拟线程内存开销 ~1KB vs 平台线程 ~1MB）
- 无 API 变更、无依赖变更
