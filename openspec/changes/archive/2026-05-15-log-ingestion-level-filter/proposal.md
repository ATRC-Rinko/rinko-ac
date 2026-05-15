## Why

`LogIngestionService` 目前只做概率采样（`shouldSample`），不检查 `log_level_configs` 表配置的日志级别阈值。用户通过管理 API 设置的 `serviceName + loggerName → level` 规则对入库流程毫无影响，所有日志全量写入 ClickHouse。需要在摄入链路中加入级别过滤——低于配置阈值的日志丢弃。

## What Changes

- `LogIngestionService` 注入 `LogLevelConfigMapper`，新增 `shouldKeepByLevel(LogMessage)` 方法
- 根据 `(message.service(), message.className())` 查询 `log_level_configs`，找到配置的阈值级别
- 级别比较：`message.level().ordinal() >= configuredLevel.ordinal()` → 保留，否则丢弃
- 使用 `ConcurrentHashMap` 缓存配置，每 30 秒通过 `@Scheduled` 刷新
- `shouldSample` + 级别过滤形成两级过滤链

## Capabilities

### Modified Capabilities

- `log-ingestion`: 摄入日志前增加级别阈值过滤，低于配置级别的日志不入库

## Impact

| 文件 | 操作 |
|------|------|
| `LogIngestionService.java` | 注入 `LogLevelConfigMapper`，新增级别过滤 + 定时刷新缓存 |
