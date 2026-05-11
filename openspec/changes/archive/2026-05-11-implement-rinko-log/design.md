## Context

`rinko-log` 是日志体系的中心服务。各微服务通过 `rinko-infra` 的 `JsonEncoder` 将日志以结构化 JSON 格式输出到文件/控制台，同时通过 Logback `KafkaAppender` 或服务自身的 Kafka 生产者投递到 Kafka Topic。`rinko-log` 消费 Kafka 日志消息，批量写入 ClickHouse，并提供查询 API 和动态日志级别管理。

当前基础设施（Kafka + ClickHouse）已在 `docker-compose.yml` 中配置，`JsonEncoder`/`TraceIdConverter` 已在 `rinko-infra` 实现。本项目采用 **Servlet + Jetty + JDBC** 技术栈。

## Goals / Non-Goals

**Goals:**
- 实现 Kafka 消费者监听日志 Topic，批量写入 ClickHouse
- 暴露日志查询 REST API：按时间段、级别、服务名、traceId 过滤，支持分页
- 暴露动态日志级别 API：修改单个服务的 Logger 级别，通过 Spring Cloud Bus 广播
- 使用 Flyway 管理 PostgreSQL 元数据（动态日志级别配置表）
- ClickHouse 建表通过 SQL 迁移脚本管理

**Non-Goals:**
- 不实现日志告警规则引擎
- 不实现日志可视化 Dashboard（由 SkyWalking UI 覆盖）
- 实现日志采样策略（`rinko.log.sampling-rate`），支持按比例丢弃低级别日志
- 不修改 `rinko-infra` 的 JsonEncoder

## Decisions

### 1. ClickHouse 连接方式：JDBC

**决策**: 使用 ClickHouse 官方 JDBC 驱动 (`com.clickhouse:clickhouse-jdbc`)，通过 HTTP 协议连接（端口 8123）。

**理由**: Servlet 模块使用阻塞 JDBC 是自然选择；ClickHouse JDBC 驱动成熟稳定，支持 `PreparedStatement` 批量插入，性能足够（异步插入场景可后续优化）。HTTP 端口已在 docker-compose 中暴露（8123）。

**替代方案**: 使用 ClickHouse 原生 TCP 客户端（端口 9000）→ JDBC HTTP 驱动足够，且免去额外二进制协议依赖。

### 2. Kafka 消费模式：Spring Kafka + 批量消费

**决策**: 使用 `spring-kafka` 的 `@KafkaListener`，配置 `batch` 模式监听。每次拉取一批日志消息，通过 ClickHouse JDBC batch insert 一次性写入。

**理由**: ClickHouse 针对批量写入优化（建议每批 1000-10000 行）。逐条写入性能极差。Spring Kafka 自动管理 offset 提交（`enable.auto.commit=true`）。

### 3. ClickHouse 表设计：分区按日，排序键 (timestamp, service)

**决策**:
```sql
CREATE TABLE logs (
    timestamp DateTime64(3, 'Asia/Shanghai'),
    level String,
    service String,
    traceId String,
    spanId String,
    class String,
    message String,
    thread String,
    context String,       -- JSON string of MDC context
    exception String,
    exceptionClass String
) ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp, service);
```

**理由**: 分区按日便于 TTL 管理和旧数据删除；ORDER BY (timestamp, service) 覆盖最常见的查询模式（按时间 + 服务过滤）。

### 4. 动态日志级别实现：Spring Cloud Bus + Actuator

**决策**: 提供一个 REST API 修改指定服务的 Logger 级别，通过 Spring Cloud Bus 发送 `RemoteApplicationEvent` 到目标服务，目标服务监听事件后调用 Logback 动态修改 Logger 级别。

日志级别配置持久化到 PostgreSQL（通过 Flyway 建表），服务重启时恢复。

### 5. 日志采样策略：`rinko.log.sampling-rate`

**决策**: 提供 `rinko.log.sampling-rate` 配置属性，取值 0.0—1.0（默认 1.0 = 全量），在消费者侧按比例丢弃低级别日志（INFO、DEBUG、TRACE），ERROR 和 WARN 始终全量保留。

**理由**: 生产环境日志量大时，Kafka → ClickHouse 链条可能因带宽/磁盘压力成为瓶颈。采样策略允许运维在不丢失关键日志的前提下降低存储成本。

### 6. 包结构：遵循 coding-standards

模块包结构：`com.rinko.log.{config,controller,dto,entity,repository,service,consumer}`。

## Risks / Trade-offs

- **[风险] ClickHouse 批量插入失败导致日志丢失** → Kafka 消费者不自动提交 offset，批量写入成功后再手动提交
- **[风险] ClickHouse 连接不可用时服务启动失败** → ClickHouse 连接配置 `health-check` 但允许启动；Kafka 消费者在 ClickHouse 恢复后自动重连
