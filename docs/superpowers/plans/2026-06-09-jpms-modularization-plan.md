# JPMS 全量模块化实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为所有 7 个 Maven 子模块添加 `module-info.java`，清理 `--add-opens` 遗留标志

**Architecture:** 自底向上：先 `rinko-infra`（无内部依赖），再 6 个应用模块。每个 `module-info.java` 通过 `mvn compile` 迭代验证，利用 javac 错误信息校准自动模块名。最终移除 `.mvn/jvm.config`、`pom.xml` surefire 和 `docker-compose.yml` 中的 4 处 `--add-opens`

**Tech Stack:** Java 21, Spring Boot 4.0.5, Maven, Kotlin 2.1.0, JPMS

**设计文档:** `docs/superpowers/specs/2026-06-09-jpms-modularization-design.md`

---

## 模块名速查表（已通过 `jar --describe-module` 验证）

| 外部 JAR | 模块名 | 类型 |
|---|---|---|
| spring-boot-4.0.5.jar | `spring.boot` | automatic |
| spring-boot-autoconfigure-4.0.5.jar | `spring.boot.autoconfigure` | automatic |
| spring-context-7.0.6.jar | `spring.context` | automatic |
| spring-web-7.0.6.jar | `spring.web` | automatic |
| reactor-core-3.8.4.jar | `reactor.core` | automatic |
| jackson-databind-3.1.0.jar | `tools.jackson.databind` | named |
| jakarta.servlet-api-6.1.0.jar | `jakarta.servlet` | named |
| caffeine-3.1.8.jar | `com.github.benmanes.caffeine` | named |
| jjwt-api-0.12.6.jar | `jjwt.api` | automatic |
| druid-spring-boot-4-starter-1.2.28.jar | `druid.spring.boot4.starter` | named |
| mybatis-plus-spring-boot4-starter-3.5.16.jar | `com.baomidou.mybatis.plus.spring.boot4.starter` | automatic |
| springdoc-openapi-starter-webmvc-ui-2.6.0.jar | `org.springdoc.openapi.ui` | automatic |
| springdoc-openapi-starter-webflux-ui-2.6.0.jar | `org.springdoc.openapi.webflux.ui` | automatic |
| s3-2.29.52.jar | `software.amazon.awssdk.services.s3` | automatic |
| nacos-discovery-2025.1.0.0.jar | `spring.cloud.starter.alibaba.nacos.discovery` | automatic |
| r2dbc-postgresql-1.0.6.RELEASE.jar | `r2dbc.postgresql` | automatic |
| kotlin-stdlib-2.1.0.jar | `kotlin.stdlib` | filename-derived |
| kotlin-reflect-2.1.0.jar | `kotlin.reflect` | filename-derived |

> 其他未缓存的依赖将在首次 `mvn compile` 时由 javac 报错信息指出确切的自动模块名。

---

### Task 1: 一键发现所有外部模块名

**Files:**
- Create: (无 — 仅运行脚本)

- [ ] **Step 1: 运行模块发现脚本**

```bash
cd /d/work/c-web/rinko-ac
mvn dependency:resolve -q 2>&1
```

确认所有依赖已下载到 `~/.m2/repository`。

- [ ] **Step 2: 批量查询自动模块名**

```bash
REPO="$HOME/.m2/repository"
for jar in \
  "org/springframework/boot/spring-boot-starter/4.0.5/spring-boot-starter-4.0.5.jar" \
  "org/springframework/spring-webflux/7.0.6/spring-webflux-7.0.6.jar" \
  "org/springframework/spring-webmvc/7.0.6/spring-webmvc-7.0.6.jar" \
  "org/springframework/boot/spring-boot-starter-jetty/4.0.5/spring-boot-starter-jetty-4.0.5.jar" \
  "org/springframework/boot/spring-boot-starter-security/4.0.8/spring-boot-starter-security-4.0.8.jar" \
  "org/springframework/boot/spring-boot-starter-data-r2dbc/4.0.5/spring-boot-starter-data-r2dbc-4.0.5.jar" \
  "org/springframework/boot/spring-boot-starter-data-redis-reactive/4.0.5/spring-boot-starter-data-redis-reactive-4.0.5.jar" \
  "org/springframework/boot/spring-boot-starter-data-redis/4.0.5/spring-boot-starter-data-redis-4.0.5.jar" \
  "org/springframework/boot/spring-boot-starter-amqp/4.0.5/spring-boot-starter-amqp-4.0.5.jar" \
  "org/springframework/boot/spring-boot-starter-mail/4.0.5/spring-boot-starter-mail-4.0.5.jar" \
  "org/springframework/boot/spring-boot-starter-quartz/4.0.5/spring-boot-starter-quartz-4.0.5.jar" \
  "org/springframework/boot/spring-boot-starter-kafka/4.0.5/spring-boot-starter-kafka-4.0.5.jar" \
  "org/springframework/cloud/spring-cloud-starter-gateway-server-webflux/4.2.0/spring-cloud-starter-gateway-server-webflux-4.2.0.jar" \
  "org/springframework/cloud/spring-cloud-starter-loadbalancer/4.2.0/spring-cloud-starter-loadbalancer-4.2.0.jar" \
  "com/alibaba/cloud/spring-cloud-starter-alibaba-nacos-config/2025.1.0.0/spring-cloud-starter-alibaba-nacos-config-2025.1.0.0.jar" \
  "com/alibaba/cloud/spring-cloud-starter-alibaba-seata/2025.1.0.0/spring-cloud-starter-alibaba-seata-2025.1.0.0.jar" \
  "org/springframework/cloud/spring-cloud-starter-bus-amqp/4.2.0/spring-cloud-starter-bus-amqp-4.2.0.jar" \
  "io/jsonwebtoken/jjwt-impl/0.12.6/jjwt-impl-0.12.6.jar" \
  "io/jsonwebtoken/jjwt-jackson/0.12.6/jjwt-jackson-0.12.6.jar" \
  "org/postgresql/postgresql/42.7.10/postgresql-42.7.10.jar" \
  "org/apache/commons/commons-lang3/3.19.0/commons-lang3-3.19.0.jar" \
  "org/apache/kafka/kafka-clients/3.9.1/kafka-clients-3.9.1.jar" \
  "com/clickhouse/clickhouse-jdbc/0.9.8/clickhouse-jdbc-0.9.8.jar"
do
  name=$(basename "$jar")
  echo "=== $name ==="
  jar --describe-module --file="$REPO/$jar" 2>&1 | head -3
done
```

将输出记录到模块名速查表中，补充缺失项。如果 JAR 不存在对应版本，用 `ls "$REPO/<groupId>/<artifactId>/"` 查找实际版本。

- [ ] **Step 3: 记录全部模块名到注释中**

确认每个外部依赖都有明确模块名后，进入 Task 2。

---

### Task 2: rinko-infra 的 module-info.java（基础模块）

**Files:**
- Create: `rinko-infra/src/main/java/module-info.java`

- [ ] **Step 1: 创建 module-info.java**

```java
module com.rinko.infra {
    // Public API — 供其他 rinko 模块使用
    exports com.rinko.infra.config;
    exports com.rinko.infra.dto;
    exports com.rinko.infra.id;
    exports com.rinko.infra.log;
    exports com.rinko.infra.web;

    // 框架依赖
    requires spring.boot;
    requires spring.context;
    requires spring.web;
    requires tools.jackson.databind;
    requires org.apache.commons.lang3;
    requires kotlin.stdlib;
    requires druid.spring.boot4.starter;
    requires apm.toolkit.trace;
    requires org.apache.kafka.clients;

    // Optional: 仅在 consumer 模块引入了 webmvc 或 reactor 时存在
    requires static spring.webmvc;
    requires static reactor.core;
    requires static jakarta.servlet;
}
```

- [ ] **Step 2: 编译验证 rinko-infra**

```bash
mvn clean compile -pl rinko-infra -am
```

**预期:** 如果编译失败，javac 会报告 "module not found: X" 或 "package Y is not visible"。根据实际错误调整 `requires` 语句中的模块名。最常见的修正是自动模块名与假设不同——用实际模块名替换。

- [ ] **Step 3: 迭代修正直到编译通过**

典型修正流程：
1. 错误: `package org.springframework.boot is not visible` → 检查 `spring.boot` 是否在 `requires` 中
2. 错误: `module not found: spring.webmvc` → 验证 JAR 实际模块名，可能为 `spring.webmvc` 或 `java.servlet` 等不同命名
3. 如果某个包来自传递依赖且 infra 不需要直接引用，移除对应的 `requires`

- [ ] **Step 4: 验证模块描述符**

```bash
jar --describe-module --file=rinko-infra/target/rinko-infra-1.0.0-SNAPSHOT.jar
```

确认输出中能看到 `exports com.rinko.infra.config` 等导出声明。

---

### Task 3-8: 应用模块 module-info.java（6 个模块，每个类似流程）

以下模板适用于每个应用模块。关键差异：Kotlin 模块（gateway、auth）需要额外处理。

#### 通用应用模块模板

**Files:**
- Create: `<module>/src/main/java/module-info.java`

**Step 1: 创建 module-info.java**

```java
// 以 rinko-oss 为例
module com.rinko.oss {
    requires com.rinko.infra;

    // Spring Boot Web (Servlet)
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.web;
    requires spring.webmvc;
    requires spring.context;

    // Jetty
    requires spring.boot.starter.jetty;

    // Nacos
    requires spring.cloud.starter.alibaba.nacos.discovery;
    requires spring.cloud.starter.alibaba.nacos.config;
    requires com.alibaba.cloud.seata;

    // Database
    requires druid.spring.boot4.starter;
    requires postgresql;
    requires com.baomidou.mybatis.plus.spring.boot4.starter;

    // S3
    requires software.amazon.awssdk.services.s3;

    // OpenAPI
    requires org.springdoc.openapi.ui;

    // MyBatis 实体反射 — 精确开放给 MyBatis-Plus
    opens com.rinko.oss.model.entity to com.baomidou.mybatis.plus.spring.boot4.starter;
    // Controller 层反射 — 精确开放给 Spring Web
    opens com.rinko.oss.controller to spring.web, spring.webmvc;
}
```

**Step 2: 编译验证**

```bash
mvn clean compile -pl rinko-oss -am
```

**Step 3: 迭代修正直到编译通过**

同上，根据 javac 错误校准模块名。

---

### Task 3: rinko-oss 的 module-info.java

**Files:**
- Create: `rinko-oss/src/main/java/module-info.java`

完整内容如通用模板所示。具体 `requires` 列表见上。

- [ ] **Step 1-3:** 创建 → 编译 → 修正（同模板）

---

### Task 4: rinko-log 的 module-info.java

**Files:**
- Create: `rinko-log/src/main/java/module-info.java`

```java
module com.rinko.log {
    requires com.rinko.infra;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.web;
    requires spring.webmvc;
    requires spring.context;
    requires spring.boot.starter.jetty;

    requires spring.cloud.starter.alibaba.nacos.discovery;
    requires spring.cloud.starter.alibaba.nacos.config;

    // Kafka
    requires spring.boot.starter.kafka;

    // Database
    requires druid.spring.boot4.starter;
    requires postgresql;
    requires com.baomidou.mybatis.plus.spring.boot4.starter;

    // ClickHouse
    requires clickhouse.jdbc;

    // OpenAPI
    requires org.springdoc.openapi.ui;

    opens com.rinko.log.model.entity to com.baomidou.mybatis.plus.spring.boot4.starter;
    opens com.rinko.log.controller to spring.web, spring.webmvc;
}
```

- [ ] **Step 1-3:** 创建 → 编译 → 修正

---

### Task 5: rinko-notify 的 module-info.java

**Files:**
- Create: `rinko-notify/src/main/java/module-info.java`

```java
module com.rinko.notify {
    requires com.rinko.infra;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.web;
    requires spring.webmvc;
    requires spring.context;
    requires spring.boot.starter.jetty;

    requires spring.cloud.starter.alibaba.nacos.discovery;
    requires spring.cloud.starter.alibaba.nacos.config;
    requires com.alibaba.cloud.seata;

    // Mail + RabbitMQ + Redis
    requires spring.boot.starter.mail;
    requires spring.cloud.starter.bus.amqp;
    requires spring.boot.starter.data.redis;

    // Database
    requires druid.spring.boot4.starter;
    requires postgresql;
    requires com.baomidou.mybatis.plus.spring.boot4.starter;

    // OpenAPI
    requires org.springdoc.openapi.ui;

    opens com.rinko.notify.model.entity to com.baomidou.mybatis.plus.spring.boot4.starter;
    opens com.rinko.notify.controller to spring.web, spring.webmvc;
}
```

- [ ] **Step 1-3:** 创建 → 编译 → 修正

---

### Task 6: rinko-scheduler 的 module-info.java

**Files:**
- Create: `rinko-scheduler/src/main/java/module-info.java`

```java
module com.rinko.scheduler {
    requires com.rinko.infra;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.web;
    requires spring.webmvc;
    requires spring.context;
    requires spring.boot.starter.jetty;

    requires spring.cloud.starter.alibaba.nacos.discovery;
    requires spring.cloud.starter.alibaba.nacos.config;
    requires com.alibaba.cloud.seata;

    // Quartz + RabbitMQ
    requires spring.boot.starter.quartz;
    requires spring.cloud.starter.bus.amqp;

    // Database
    requires druid.spring.boot4.starter;
    requires postgresql;
    requires com.baomidou.mybatis.plus.spring.boot4.starter;

    // OpenAPI
    requires org.springdoc.openapi.ui;

    opens com.rinko.scheduler.model.entity to com.baomidou.mybatis.plus.spring.boot4.starter;
    opens com.rinko.scheduler.controller to spring.web, spring.webmvc;
}
```

- [ ] **Step 1-3:** 创建 → 编译 → 修正

---

### Task 7: rinko-gateway 的 module-info.java（Kotlin + WebFlux）

**Files:**
- Create: `rinko-gateway/src/main/java/module-info.java`

- [ ] **Step 1: 创建 module-info.java**

```java
module com.rinko.gateway {
    requires com.rinko.infra;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.web;
    requires spring.webflux;

    // Spring Cloud Gateway
    requires spring.cloud.starter.gateway.server.webflux;
    requires spring.cloud.loadbalancer;

    // Kotlin
    requires kotlin.stdlib;
    requires kotlin.reflect;

    // JWT
    requires jjwt.api;

    // Nacos
    requires spring.cloud.starter.alibaba.nacos.discovery;
    requires spring.cloud.starter.alibaba.nacos.config;

    // Gateway filter chain 反射
    opens com.rinko.gateway.config to spring.boot, spring.boot.autoconfigure;
    opens com.rinko.gateway.filter to spring.web, spring.webflux;
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn clean compile -pl rinko-gateway -am
```

- [ ] **Step 3: 迭代修正直到编译通过**

Kotlin 特有的注意点：
- Kotlin 编译产物在 `target/classes/` 与 `module-info.class` 混合
- 如果出现 "package com.rinko.gateway.config is empty"，检查 Kotlin 源码是否编译成功
- `kotlin-maven-plugin` 在 `compile` 阶段先于 `maven-compiler-plugin` 执行，Kotlin `.class` 应该已经存在

---

### Task 8: rinko-auth 的 module-info.java（Kotlin + WebFlux）

**Files:**
- Create: `rinko-auth/src/main/java/module-info.java`

- [ ] **Step 1: 创建 module-info.java**

```java
module com.rinko.auth {
    requires com.rinko.infra;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.web;
    requires spring.webflux;

    // R2DBC + Redis Reactive
    requires spring.boot.starter.data.r2dbc;
    requires spring.boot.starter.data.redis.reactive;
    requires r2dbc.postgresql;

    // Security
    requires spring.boot.starter.security;

    // Kotlin
    requires kotlin.stdlib;
    requires kotlin.reflect;

    // JWT
    requires jjwt.api;

    // Caffeine
    requires com.github.benmanes.caffeine;

    // Nacos + Seata + RabbitMQ
    requires spring.cloud.starter.alibaba.nacos.discovery;
    requires spring.cloud.starter.alibaba.nacos.config;
    requires com.alibaba.cloud.seata;
    requires spring.boot.starter.amqp;

    // OpenAPI
    requires org.springdoc.openapi.webflux.ui;

    // Spring Security + 实体反射
    opens com.rinko.auth.entity to spring.boot.starter.security, spring.security.core;
    opens com.rinko.auth.controller to spring.web, spring.webflux;
    opens com.rinko.auth.config to spring.boot, spring.boot.autoconfigure;
    opens com.rinko.auth.security to spring.boot.starter.security;
}
```

- [ ] **Step 2-3:** 编译 → 修正（同 Task 7）

---

### Task 9: 清理 .mvn/jvm.config

**Files:**
- Modify: `.mvn/jvm.config`

- [ ] **Step 1: 替换文件内容**

将原文件：
```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.lang.reflect=ALL-UNNAMED
--add-opens java.base/java.lang.invoke=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
-XX:+EnableDynamicAgentLoading
```

替换为仅保留 agent 加载：
```
-XX:+EnableDynamicAgentLoading
```

- [ ] **Step 2: 验证 Maven 仍可启动**

```bash
mvn --version
```

预期: 正常输出版本信息。

---

### Task 10: 清理 pom.xml surefire argLine

**Files:**
- Modify: `pom.xml:311-313`

- [ ] **Step 1: 移除 surefire 的 add-opens**

在 `pom.xml` 中，将 maven-surefire-plugin 配置从：
```xml
<configuration>
    <argLine>-XX:+EnableDynamicAgentLoading --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED</argLine>
</configuration>
```

替换为：
```xml
<configuration>
    <argLine>-XX:+EnableDynamicAgentLoading</argLine>
</configuration>
```

- [ ] **Step 2: 验证编译不受影响**

```bash
mvn clean compile
```

---

### Task 11: 清理 docker-compose.yml 的 --add-opens

**Files:**
- Modify: `docker-compose.yml`

- [ ] **Step 1: 移除所有应用的 JAVA_OPTS 中 --add-opens**

定位每处包含 `--add-opens` 的 `JAVA_OPTS` 块（共 6 组），每组当前为：

```yaml
- JAVA_OPTS=--add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED
```

和对应的 JSON 数组格式：
```yaml
"--add-opens", "java.base/java.lang=ALL-UNNAMED",
"--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
"--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
"--add-opens", "java.base/java.util=ALL-UNNAMED",
```

全部删除这 4 行 `--add-opens`。

**注意:** 如果 SkyWalking OAP 容器有自己的 `--add-opens`，保留不变——agent 在模块系统之前运行，需要命令行动态打开。

---

### Task 12: 全量验证

- [ ] **Step 1: 全模块编译**

```bash
mvn clean compile
```

**预期:** BUILD SUCCESS，0 错误。

- [ ] **Step 2: 运行测试**

```bash
mvn test
```

**预期:** 全部测试通过。如果有测试失败，检查失败日志：
- `InaccessibleObjectException` → 某个 `opens` 声明缺失，补充 `opens <package> to <module>` 
- `NoClassDefFoundError` → 某个 `requires` 缺失，补充模块依赖

- [ ] **Step 3: 验证最终的模块描述符**

```bash
for mod in rinko-infra rinko-gateway rinko-auth rinko-oss rinko-log rinko-notify rinko-scheduler; do
  echo "=== $mod ==="
  jar --describe-module --file="$mod/target/$mod-1.0.0-SNAPSHOT.jar" 2>&1 | head -15
done
```

确认每个模块的 jar 包含 `module-info.class` 且有正确的 `exports` 声明。

---

### Task 13: Git 提交

- [ ] **Step 1: 提交所有修改**

```bash
git add \
  rinko-infra/src/main/java/module-info.java \
  rinko-gateway/src/main/java/module-info.java \
  rinko-auth/src/main/java/module-info.java \
  rinko-oss/src/main/java/module-info.java \
  rinko-log/src/main/java/module-info.java \
  rinko-notify/src/main/java/module-info.java \
  rinko-scheduler/src/main/java/module-info.java \
  .mvn/jvm.config \
  pom.xml \
  docker-compose.yml

git commit -m "feat: add JPMS module-info.java to all 7 modules, remove --add-opens workarounds"
```

---

## 验证清单

- [ ] `mvn clean compile` 全模块通过
- [ ] `mvn test` 全部测试通过
- [ ] 每个 `target/*.jar` 包含 `module-info.class`
- [ ] `.mvn/jvm.config` 不再有 `--add-opens` 行
- [ ] `pom.xml` surefire argLine 不再有 `--add-opens`
- [ ] `docker-compose.yml` 应用容器不再有 `--add-opens`
