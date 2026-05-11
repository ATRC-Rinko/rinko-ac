## Context

JDK 21 引入了多项正式特性：Virtual Threads (JEP 444)、Record Patterns (JEP 440)、Pattern Matching for switch (JEP 441)、Sequenced Collections (JEP 431)。项目已声明 JDK 21 作为编译和运行目标，但未利用这些特性。

当前问题：
- 构建和运行时出现 `java.lang.reflect.InaccessibleObjectException` 警告（Spring、Jackson、CGLIB 反射访问 JDK 内部模块被拒绝）
- Mockito 每次执行测试都警告 "Dynamic loading of agents will be disallowed by default"
- Servlet 模块使用平台线程（~1MB/线程），高并发时存在线程资源瓶颈
- 无统一的 JVM 参数管理

## Goals / Non-Goals

**Goals:**
- 添加 `.mvn/jvm.config` 统一管理构建期 JVM 参数
- 配置必要的 `--add-opens` 消除 JDK 模块访问警告
- 配置 Mockito javaagent 消除动态加载警告
- Servlet 模块启用 Virtual Threads（`spring.threads.virtual.enabled=true`）
- Docker 运行时添加 JDK 21 模块导出参数

**Non-Goals:**
- 不将 WebFlux 模块（Kotlin）迁移到 Virtual Threads（Reactor/Netty 有独立的线程模型）
- 不引入 JDK 21 Preview 特性（如 String Templates）
- 不修改已有代码逻辑（纯配置变更）

## Decisions

### 1. Virtual Threads 仅用于 Servlet 模块

**决策**: `spring.threads.virtual.enabled=true` 仅在 Nacos 公共配置中设置，Servlet 模块（Jetty）使用虚拟线程，WebFlux 模块（Netty）不受影响。

**理由**: Spring Boot 3.2+ 的 `spring.threads.virtual.enabled` 仅影响嵌入式 Web Server 的线程池。Netty 的 event loop 不受此配置影响（Netty 有自己基于 Java NIO 的线程模型）。

### 2. `--add-opens` 选择：最小原则

**决策**: 只添加必要的 `--add-opens`，不在 `--add-exports` 和宽泛的 `ALL-UNNAMED` 之外过度开放。

必要的 `--add-opens`：
- `java.base/java.lang` — Spring CGLIB 代理
- `java.base/java.lang.reflect` — Jackson 反射
- `java.base/java.lang.invoke` — MethodHandle 访问
- `java.base/java.util` — 集合反射

### 3. Mockito 处理：JVM Agent 模式

**决策**: 在 `.mvn/jvm.config` 中添加 `-XX:+EnableDynamicAgentLoading` 参数，同时为 Surefire 插件配置 `argLine` 属性。

**理由**: Mockito 5.x 内联 mock maker 通过 ByteBuddy 动态 attach，JDK 21 默认拒绝。`-XX:+EnableDynamicAgentLoading` 在 JDK 21 中允许此行为。JDK 25 将彻底禁止，届时 Mockito 需升级。

### 4. `.mvn/jvm.config` 集中管理

**决策**: 所有 Maven 构建的 JVM 参数集中到 `.mvn/jvm.config`，每个参数一行，Maven Wrapper 自动读取。

**理由**: 避免在 `pom.xml` 的 surefire 插件配置中重复声明，确保 IDE 导入和命令行构建使用相同的 JVM 参数。

## Risks / Trade-offs

- **[风险] Virtual Threads 中 `synchronized` 块可能 pin 住载体线程** → 项目中 `synchronized` 使用场景有限，主要在 `LogIngestionService.flush()`，影响可控
- **[风险] `--add-opens` 在 JDK 25 中可能失效** → 届时需要升级 Spring Boot、Jackson 等框架到原生支持强封装的版本
- **[取舍] 不在 WebFlux 模块启用 Virtual Threads** → WebFlux 的非阻塞模型已经足够高效，强行混合 Virtual Threads 可能引入复杂性
