# JPMS 全量模块化设计

**日期**: 2026-06-09
**目标**: 为 rinko-ac 微服务项目添加完整 JPMS module-info.java，清理 --add-opens 遗留

## 当前状态

- Spring Boot 4.0.5, Java 21, Maven 多模块 (7 个子模块)
- 无 `module-info.java`
- 4 处 `--add-opens` 遗留：
  - `.mvn/jvm.config` (5 行)
  - `pom.xml` → surefire plugin argLine (1 行)
  - `docker-compose.yml` (6 组 × 4 行 = 24 行)

## 目标状态

- 7 个模块全部添加 `module-info.java`
- 移除所有 `--add-opens`（SkyWalking agent 除外）
- 编译 + 测试通过

## 模块依赖图

```
rinko-infra (com.rinko.infra.*)  ← 共享库，无 rinko 内部依赖
   ↑
   ├── rinko-gateway (Kotlin, WebFlux)
   ├── rinko-auth   (Kotlin, WebFlux)
   ├── rinko-oss    (Java, Servlet+Jetty)
   ├── rinko-log    (Java, Servlet+Jetty)
   ├── rinko-notify (Java, Servlet+Jetty)
   └── rinko-scheduler (Java, Servlet+Jetty)
```

## 模块声明清单

### rinko-infra (`com.rinko.infra`)

```
module com.rinko.infra {
    exports com.rinko.infra.config;
    exports com.rinko.infra.web;
    exports com.rinko.infra.dto;
    exports com.rinko.infra.id;
    exports com.rinko.infra.log;
    // 不导出 datasource（内部实现）

    requires spring.boot;
    requires spring.web;
    requires spring.webmvc;         // optional — 仅 rinko-oss/log/notify/scheduler 的 web 模式使用
    requires spring.context;
    requires jakarta.servlet;        // provided
    requires tools.jackson.databind;
    requires org.apache.commons.lang3;
    requires kotlin.stdlib;
    requires reactor.core;          // optional — 仅 reactive 模块使用
    requires org.apache.kafka.clients;
    requires com.alibaba.druid;
}
```

### rinko-gateway (`com.rinko.gateway`)

```
module com.rinko.gateway {
    requires com.rinko.infra;
    requires spring.cloud.gateway.server.webflux;
    requires spring.cloud.loadbalancer;
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires jjwt.api;
    requires com.alibaba.cloud.nacos.discovery;
    requires com.alibaba.cloud.nacos.config;
    // Spring Boot 自动配置需要 opens
    opens com.rinko.gateway to spring.core;
}
```

### rinko-auth (`com.rinko.auth`)

```
module com.rinko.auth {
    requires com.rinko.infra;
    requires spring.boot.starter.webflux;
    requires spring.boot.starter.data.r2dbc;
    requires spring.boot.starter.security;
    requires spring.boot.starter.data.redis.reactive;
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires jjwt.api;
    requires com.github.benmanes.caffeine;
    requires com.alibaba.cloud.nacos.discovery;
    requires com.alibaba.cloud.nacos.config;
    requires com.alibaba.cloud.seata;
    requires spring.boot.starter.amqp;
    requires r2dbc.postgresql;
    requires springdoc.openapi.webflux.ui;
    // Spring Security + MyBatis 反射需要
    opens com.rinko.auth.entity to spring.core, mybatis.plus;
    opens com.rinko.auth.controller to spring.web;
}
```

### rinko-oss (`com.rinko.oss`)

```
module com.rinko.oss {
    requires com.rinko.infra;
    requires spring.boot.starter.web;
    requires spring.boot.starter.jetty;
    requires com.alibaba.cloud.nacos.discovery;
    requires com.alibaba.cloud.nacos.config;
    requires com.alibaba.cloud.seata;
    requires com.alibaba.druid;
    requires postgresql;
    requires mybatis.plus.spring.boot4.starter;
    requires software.amazon.awssdk.s3;
    requires springdoc.openapi.webmvc.ui;
    // MyBatis 实体反射
    opens com.rinko.oss.model.entity to mybatis.plus;
    opens com.rinko.oss.controller to spring.web;
}
```

### rinko-log (`com.rinko.log`)

```
module com.rinko.log {
    requires com.rinko.infra;
    requires spring.boot.starter.web;
    requires spring.boot.starter.jetty;
    requires com.alibaba.cloud.nacos.discovery;
    requires com.alibaba.cloud.nacos.config;
    requires spring.boot.starter.kafka;
    requires com.alibaba.druid;
    requires postgresql;
    requires mybatis.plus.spring.boot4.starter;
    requires springdoc.openapi.webmvc.ui;
    requires clickhouse.jdbc;
    // MyBatis 实体反射
    opens com.rinko.log.model.entity to mybatis.plus;
    opens com.rinko.log.controller to spring.web;
}
```

### rinko-notify (`com.rinko.notify`)

```
module com.rinko.notify {
    requires com.rinko.infra;
    requires spring.boot.starter.web;
    requires spring.boot.starter.jetty;
    requires com.alibaba.cloud.nacos.discovery;
    requires com.alibaba.cloud.nacos.config;
    requires com.alibaba.cloud.seata;
    requires spring.boot.starter.mail;
    requires spring.cloud.starter.bus.amqp;
    requires spring.boot.starter.data.redis;
    requires com.alibaba.druid;
    requires postgresql;
    requires mybatis.plus.spring.boot4.starter;
    requires springdoc.openapi.webmvc.ui;
    // MyBatis 实体反射
    opens com.rinko.notify.model.entity to mybatis.plus;
    opens com.rinko.notify.controller to spring.web;
}
```

### rinko-scheduler (`com.rinko.scheduler`)

```
module com.rinko.scheduler {
    requires com.rinko.infra;
    requires spring.boot.starter.web;
    requires spring.boot.starter.jetty;
    requires com.alibaba.cloud.nacos.discovery;
    requires com.alibaba.cloud.nacos.config;
    requires com.alibaba.cloud.seata;
    requires spring.boot.starter.quartz;
    requires spring.cloud.starter.bus.amqp;
    requires com.alibaba.druid;
    requires postgresql;
    requires mybatis.plus.spring.boot4.starter;
    requires springdoc.openapi.webmvc.ui;
    // MyBatis + Quartz 反射
    opens com.rinko.scheduler.model.entity to mybatis.plus;
    opens com.rinko.scheduler.controller to spring.web;
}
```

## 清理清单

### `.mvn/jvm.config` — 全部移除

移除全部 5 行：
```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.lang.reflect=ALL-UNNAMED
--add-opens java.base/java.lang.invoke=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
-XX:+EnableDynamicAgentLoading
```

### `pom.xml` surefire argLine — 移除

移除 maven-surefire-plugin 的 `<argLine>` 配置。

### `docker-compose.yml` — 移除 6 组 × 4 行

skywalking-oap 容器保留 agent 相关 --add-opens（agent 在模块系统前运行）。

## 关键风险及缓解

| 风险 | 缓解措施 |
|------|----------|
| Kotlin 编译器不支持 module-path | `kotlin-maven-plugin` 可用 `-Xjdk-release` 或放在 classpath 编译，module-info.java 由 javac 单独编译 |
| 自动模块名猜测不稳定 | 所有 rinko 内部依赖都用显式模块名，外部依赖使用 jar `--describe-module` 确认自动模块名 |
| MyBatis-Plus 大量反射 | 用 `opens ... to mybatis.plus` 精确开放 |
| Spring Boot auto-config 加载失败 | `opens ... to spring.core` 确保 `@Configuration` 类可被 Spring 访问 |

## 验证步骤

1. `mvn clean compile -pl rinko-infra` → 通过
2. `mvn clean compile` (全模块) → 通过
3. `mvn test` → 全模块通过
4. 确认 SkyWalking agent 日志无异常
