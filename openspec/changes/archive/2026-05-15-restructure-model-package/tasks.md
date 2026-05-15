## 1. rinko-oss — 已完成框架

- [x] 1.1 确认 model.entity/model.dto/model.vo 已就位，删除旧 dto/entity 包

## 2. rinko-oss — 创建 VO 替换实体返回

- [x] 2.1 创建 `FileMetadataVO`、`FileVersionVO`、`VideoResolutionVO`
- [x] 2.2 `FileController` 返回类型改为 VO
- [x] 2.3 `VersionController` / `MediaController` 返回类型改为 VO（如存在）

## 3. rinko-log — model 包 + VO

- [x] 3.1 entity/dto 移至 model.entity/model.dto
- [x] 3.2 创建 `LogLevelConfigVO`、`LogEntryVO`
- [x] 3.3 `LogLevelController` / `LogQueryController` 返回类型改为 VO

## 4. rinko-notify — model 包 + VO

- [x] 4.1 entity/dto 移至 model.entity/model.dto
- [x] 4.2 创建 `NotificationHistoryVO`、`NotificationTemplateVO`
- [x] 4.3 `TemplateController` / `NotifyController` / `InboxController` 返回类型改为 VO

## 5. rinko-scheduler — model 包 + VO

- [x] 5.1 entity/dto 移至 model.entity/model.dto
- [x] 5.2 创建 `SchedulerJobVO`、`SchedulerExecutionVO`、`SchedulerDependencyVO`
- [x] 5.3 `SchedulerController` / `DagController` 返回类型改为 VO

## 6. rinko-auth — Kotlin model 包

- [x] 6.1 entity 移至 model.entity
- [x] 6.2 `AuthController` 等返回类型检查

## 7. 全量构建验证

- [x] 7.1 `mvn compile -DskipTests` 成功
