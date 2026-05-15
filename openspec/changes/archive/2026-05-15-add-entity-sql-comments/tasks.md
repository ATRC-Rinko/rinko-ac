## 1. rinko-log 实体

- [x] 1.1 `LogLevelConfig.java` 字段注释
- [x] 1.2 `LogEntry.java` 字段注释

## 2. rinko-notify 实体

- [x] 2.1 `NotificationHistory.java` 字段注释
- [x] 2.2 `NotificationTemplate.java` 字段注释

## 3. rinko-oss 实体

- [x] 3.1 `FileMetadata.java` 字段注释
- [x] 3.2 `FileVersion.java` 字段注释
- [x] 3.3 `VideoResolutionEntity.java` 字段注释

## 4. rinko-scheduler 实体

- [x] 4.1 `SchedulerJob.java` 字段注释
- [x] 4.2 `SchedulerExecution.java` 字段注释
- [x] 4.3 `SchedulerDependency.java` 字段注释

## 5. SQL 建表脚本 — 表/列注释

- [x] 5.1 rinko-log: `V1__create_log_level_configs.sql`
- [x] 5.2 rinko-notify: `V1__create_notification_history.sql`
- [x] 5.3 rinko-notify: `V2__create_notification_templates.sql`
- [x] 5.4 rinko-oss: `V1__create_file_metadata.sql`
- [x] 5.5 rinko-oss: `V2__create_file_versions.sql`
- [x] 5.6 rinko-oss: `V3__create_video_resolutions.sql`
- [x] 5.7 rinko-scheduler: `V1__create_scheduler_jobs.sql`
- [x] 5.8 rinko-scheduler: `V2__create_scheduler_executions.sql`
- [x] 5.9 rinko-scheduler: `V3__create_scheduler_dependencies.sql`

## 6. 构建验证

- [x] 6.1 `mvn compile -DskipTests` 成功
