## Context

`rinko-infra` 已有 `JsonEncoder`（`com.rinko.infra.log.JsonEncoder`）将 Logback `ILoggingEvent` 格式化为 JSON 字节流，JSON 结构与 `LogMessage` DTO 完全匹配。但该 Encoder 目前只被 CONSOLE/FILE appender 使用。

rinko-log 已有 `LogKafkaConsumer` 监听 `rinko-logs` 主题并消费 `LogMessage`，管道消费端完整但无数据来源。

## Goals / Non-Goals

**Goals:**
- 在 `rinko-infra` 中创建 `KafkaLogAppender`，使用 `KafkaTemplate` 将格式化后的日志 JSON 发送到 `rinko-logs`
- 所有业务模块的 `logback-spring.xml` 配置 KAFKA appender
- rinko-log 自身不添加此 appender（避免日志循环）

**Non-Goals:**
- 不改造 `LogKafkaConsumer` 或 `LogIngestionService`
- 不改动 ClickHouse 写入逻辑
- 不创建新的 Kafka 主题

## Decisions

1. **Appender 放在 rinko-infra**: `KafkaLogAppender` 作为共享组件，所有模块通过 Logback 配置引用。Appender 通过 Spring 上下文获取 `KafkaTemplate`——使用静态 holder 模式或 `@Component` + Logback 配置中的 class 引用。

2. **获取 KafkaTemplate 的方式**: Logback Appender 不在 Spring 容器内管理，使用静态 holder 桥接：
   ```java
   @Component
   public class KafkaLogAppenderHolder {
       private static volatile KafkaTemplate<String, String> kafkaTemplate;
       // setter via @Autowired or @PostConstruct
   }
   ```
   `KafkaLogAppender extends AppenderBase<ILoggingEvent>` 从 holder 获取 `KafkaTemplate`。

3. **KafkaTemplate 配置**: 在 rinko-infra 中添加 `KafkaProducerConfig` 配置类，创建 `KafkaTemplate<String, String>` bean。业务模块只需在 Nacos 中配置 `spring.kafka.bootstrap-servers`。

4. **只配置业务模块**: rinko-auth、rinko-gateway、rinko-oss、rinko-scheduler 添加 KAFKA appender。rinko-log 不加（自身日志不走 Kafka）。rinko-notify 已有自己的 Kafka 使用，按需配置。

5. **错误的日志记录不重试**: Appender 中 Kafka 发送失败以 ERROR 级别记录到 Logback 内部状态，不抛异常。

## Risks / Trade-offs

- **Risk: 日志循环** — rinko-log 自身的日志如果通过 Kafka 发送，消费者处理时又产生日志，形成循环。**Mitigation: rinko-log 的 logback-spring.xml 不添加 KAFKA appender。**

- **Risk: Kafka 不可用时所有日志丢失** — Appender 默认异步发送，Kafka 挂掉时日志不会落盘。**Mitigation: 保留 CONSOLE + FILE appender 作为持久化备份；KAFKA appender 的日志丢失范围可控。**
