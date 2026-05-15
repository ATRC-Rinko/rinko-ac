## Context

`confluentinc/cp-kafka` 镜像支持 `KAFKA_CREATE_TOPICS` 环境变量在容器启动时预创建 topic，格式为 `topic:partitions:replicas`。当前 `KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"` 依赖第一次请求时自动创建，存在首次超时问题。

## Goals / Non-Goals

**Goals:**
- Kafka 启动时预创建 `rinko-logs` topic（1 分区 1 副本）

**Non-Goals:**
- 不修改 Kafka 生产者/消费者代码
- 不修改 Kafka 集群配置

## Decisions

1. **1 分区 + 1 副本**: 开发/单机环境，无需多分区/多副本。生产环境按需调整。

2. **使用 `KAFKA_CREATE_TOPICS` 而非 `auto.create.topics.enable`**: 显式创建更可靠，无首次延迟。
