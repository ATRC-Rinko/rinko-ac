## Why

当前 rinko-log 只有 Kafka 消费者（`LogKafkaConsumer` 监听 `rinko-logs` 主题），但没有任何模块将日志发送到该主题。所有模块的 `logback-spring.xml` 只配置了 CONSOLE 和 FILE appender，日志无法进入 ClickHouse。需要在 `rinko-infra` 中添加共享的 `KafkaLogAppender`，让所有业务模块的 Logback 日志自动发送到 Kafka。

## What Changes

- `rinko-infra` 新增 `KafkaLogAppender`（扩展 `AppenderBase<ILoggingEvent>`），复用已有的 `JsonEncoder` 格式化为 JSON，通过 `KafkaTemplate` 发送到 `rinko-logs`
- `rinko-infra/pom.xml` 新增 `spring-kafka` 依赖（所有模块继承）
- 4 个业务模块的 `logback-spring.xml` 新增 `KAFKA` appender 配置（rinko-auth、rinko-gateway、rinko-oss、rinko-scheduler）
- rinko-notify 已使用 Kafka，配置独立处理
- `rinko-log` 已有自己的 appender（CONSOLE + FILE），不再添加 KAFKA（避免循环）
- 各模块 Nacos 配置中添加 `spring.kafka.bootstrap-servers`

## Capabilities

### New Capabilities

- `log-producer`: 所有业务模块的 Logback 日志通过 `KafkaLogAppender` 自动发送到 Kafka `rinko-logs` 主题，供 rinko-log 消费并写入 ClickHouse。

### Modified Capabilities

- `log-ingestion`: 日志数据来源从「无」变为「各模块通过 Kafka 上报」——消费端代码不变，但管道变为完整。

## Impact

| 文件 | 操作 |
|------|------|
| `rinko-infra/pom.xml` | 新增 `spring-kafka` 依赖 |
| `rinko-infra/.../log/KafkaLogAppender.java` | 新建，Logback Appender → KafkaTemplate |
| `rinko-auth/.../logback-spring.xml` | 新增 KAFKA appender |
| `rinko-gateway/.../logback-spring.xml` | 新增 KAFKA appender |
| `rinko-oss/.../logback-spring.xml` | (可能已有，需确认) 新增 KAFKA appender |
| `rinko-scheduler/.../logback-spring.xml` | 新增 KAFKA appender |
| Nacos 配置（各模块） | 新增 `spring.kafka.bootstrap-servers` |
