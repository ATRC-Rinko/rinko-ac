## Context

Spring Boot 的 `spring-boot-starter-kafka` 已包含 `KafkaAutoConfiguration`，当 `spring.kafka.bootstrap-servers` 配置存在时自动创建 `KafkaTemplate<String, String>` Bean。无需自定义 `ProducerFactory`。

## Goals / Non-Goals

**Goals:**
- 删除自定义 `KafkaProducerConfig`，依赖 Spring Boot 自动配置
- Producer 参数迁移到 yml 配置

**Non-Goals:**
- 不改变 `KafkaLogAppender` 的发送逻辑

## Decisions

1. **Producer 参数通过 `spring.kafka.producer` 配置**: Spring Boot 自动将 `spring.kafka.producer.properties.*` 映射为 Kafka producer config。
2. **Holder 简化**: 移除 `@AutoConfigureAfter` 和 `@PostConstruct`，直接用构造器注入自动配置的 `KafkaTemplate`。
