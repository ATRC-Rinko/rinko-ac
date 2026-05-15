## Why

`log_level_configs` 表为空时 `LogIngestionService.shouldKeepByLevel()` 无配置即全量保留。需要一份基础种子数据，为各模块核心包设置合理默认级别，抑制 Spring/Kafka 框架日志噪音。

## What Changes

- 新增 `db/seed/V1__seed_log_level_configs.sql` — 21 条初始配置

## Capabilities

None — 数据脚本，无代码变更。
