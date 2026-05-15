## Why

当前项目存在以下问题：
1. 部分 Controller 仍接收 `Map` 或实体类作为请求体
2. 大部分 Controller 直接返回实体类（暴露表结构）
3. DTO/VO/Entity 分散在 `dto`/`entity` 平级包下，缺乏统一管理

需要统一规范：请求走 DTO，响应走 VO，所有模型类归入 `model` 包。

## What Changes

1. **包结构调整**: 各模块 `entity`/`dto`/`vo` 移至 `model.entity`/`model.dto`/`model.vo`
2. **Controller 入参**: 全部使用 DTO（已完成大部分，收尾检查）
3. **Controller 返回值**: 全部使用 VO，禁止直接返回实体类
4. **rinko-auth Kotlin 模块**: 同步调整包结构

## Impact

| 模块 | 包调整 | 新增 VO |
|------|--------|---------|
| rinko-log | model.entity/dto/vo | LogLevelConfigVO, LogEntryVO |
| rinko-notify | model.entity/dto/vo | NotificationHistoryVO, NotificationTemplateVO |
| rinko-oss | model.entity/dto/vo | FileMetadataVO, FileVersionVO, VideoResolutionVO |
| rinko-scheduler | model.entity/dto/vo | SchedulerJobVO, SchedulerExecutionVO, SchedulerDependencyVO |
| rinko-auth | model.entity (Kotlin) | 按需 |

## Capabilities

None — 纯结构重构，无行为变更。
