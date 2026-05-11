## Why

`rinko-log` 是 Rinko 日志体系的核心服务，负责接收各微服务通过 Kafka 投递的结构化 JSON 日志并写入 ClickHouse，同时提供日志查询和动态日志级别管理能力。当前该模块完全是骨架状态（仅 pom.xml + application.yml），`rinko-infra` 中的 `JsonEncoder`/`TraceIdConverter` 已经就绪，`docker-compose.yml` 中 ClickHouse 和 Kafka 已配置，基础设施已完备——需要实现 rinko-log 的业务功能。

## What Changes

- 实现 Kafka 消费者：监听日志 Topic，反序列化 JSON 日志，批量写入 ClickHouse
- 实现 ClickHouse 集成：建表（分区按日、排序键按 timestamp + service）、批量插入 Repository
- 实现日志查询 REST API：支持按时间范围、日志级别、服务名、traceId 等条件分页查询
- 实现动态日志级别管理：通过 API 修改运行中服务的日志级别，经 Spring Cloud Bus 广播生效
- 添加 Kafka、ClickHouse、Spring Cloud Bus 相关 Maven 依赖
- 创建 Flyway 迁移脚本（PostgreSQL 存储动态日志级别配置）
- 在 pom.xml 中添加 ClickHouse JDBC 驱动和 Spring Kafka 依赖

## Capabilities

### New Capabilities

- `log-ingestion`: Kafka 日志消费与 ClickHouse 批量写入
- `log-query`: 日志查询 API — 多条件过滤、分页、按时间范围查询
- `log-level-management`: 动态日志级别管理 — REST API 修改 + Spring Cloud Bus 广播

### Modified Capabilities

<!-- 无需修改已有 capability -->

## Impact

- 影响文件：
  - `rinko-log/pom.xml` — 添加 Kafka、ClickHouse JDBC、Spring Cloud Bus 依赖
  - `rinko-log/src/main/java/com/rinko/log/` — 全新实现（~15-20 个 Java 文件）
  - `rinko-log/src/main/resources/db/migration/` — Flyway 迁移脚本
- 依赖新增：
  - `spring-kafka` — Kafka 消费者
  - `clickhouse-jdbc` — ClickHouse JDBC 驱动
  - `spring-cloud-starter-bus-amqp` — 配置刷新广播
- 基础设施：Kafka + ClickHouse（已在 docker-compose.yml 中）
