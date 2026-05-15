## Why

Kafka producer `TimeoutException: Topic rinko-logs not present in metadata` — topic 已存在但 metadata 拉取超时。`KafkaProducerConfig` 中 `request.timeout.ms=3000` 在 Docker 网络环境下首包 DNS + TCP + Metadata 耗时超过 3 秒，`max.block.ms=10000` 也偏短。需要调大超时参数并预创建 topic。

## What Changes

- `docker-compose.yml`: 添加 `KAFKA_CREATE_TOPICS: rinko-logs:1:1` 确保 topic 预创建
- `KafkaProducerConfig.java`: 调大网络超时参数以适应 Docker 网络延迟

## Capabilities

None.
