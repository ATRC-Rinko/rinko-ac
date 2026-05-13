## 1. Parent POM — Version Management

- [x] 1.1 Add `<mybatis-plus.version>` and `<pagehelper.version>` properties to parent `pom.xml`
- [x] 1.2 Add `mybatis-plus-spring-boot4-starter` and `pagehelper-spring-boot-starter` to `<dependencyManagement>` in parent `pom.xml`

## 2. rinko-log — Dependencies

- [x] 2.1 Add `com.baomidou:mybatis-plus-spring-boot4-starter` dependency to `rinko-log/pom.xml`
- [x] 2.2 Add `com.github.pagehelper:pagehelper-spring-boot-starter` dependency to `rinko-log/pom.xml`
- [x] 2.3 Remove `org.springframework.boot:spring-boot-starter-data-jdbc` dependency from `rinko-log/pom.xml`

## 3. rinko-log — Entity

- [x] 3.1 Remove `@Table` and `@Id` Spring Data annotations from `LogLevelConfig.java`
- [x] 3.2 Add `@TableName("log_level_configs")` MyBatis-Plus annotation and `@TableId(type = IdType.INPUT)` on the `id` field

## 4. rinko-log — MyBatis-Plus Mapper

- [x] 4.1 Create `LogLevelConfigMapper.java` interface extending `BaseMapper<LogLevelConfig>` with `@Mapper`

## 5. rinko-log — Configuration

- [x] 5.1 Add MyBatis-Plus config to `application.yml`
- [x] 5.2 Add PageHelper config to `application.yml`

## 6. rinko-log — Service

- [x] 6.1 Update `LogLevelManagementService.java` — replace `LogLevelConfigRepository` with `LogLevelConfigMapper`
- [x] 6.2 Update `getAllConfigs()` — use `selectList(null)`
- [x] 6.3 Update `setLogLevel()` — use `LambdaQueryWrapper` + `insert()`/`updateById()`
- [x] 6.4 Update `resetLogLevel()` — use `LambdaQueryWrapper` + `deleteById()`

## 7. rinko-log — Cleanup

- [x] 7.1 Delete `LogLevelConfigRepository.java`

## 8. rinko-log — Verification

- [x] 8.1 `mvn compile` succeeds
- [x] 8.2 BaseMapper methods cover all previous CrudRepository operations
- [x] 8.3 No autoconfiguration warnings

## 9. rinko-notify — Dependencies

- [x] 9.1 Replace `org.mybatis.spring.boot:mybatis-spring-boot-starter` with `com.baomidou:mybatis-plus-spring-boot4-starter` in `rinko-notify/pom.xml`
- [x] 9.2 Remove `org.springframework.boot:spring-boot-starter-data-jdbc` from `rinko-notify/pom.xml`

## 10. rinko-notify — Entity Annotations

- [x] 10.1 `NotificationHistory.java`: Replace `@Table` → `@TableName("notification_history")`, `@Id` → `@TableId(type = IdType.INPUT)`
- [x] 10.2 `NotificationTemplate.java`: Replace `@Table` → `@TableName("notification_templates")`, `@Id` → `@TableId(type = IdType.INPUT)`

## 11. rinko-notify — Configuration

- [x] 11.1 Update `application.yml`: `mybatis` → `mybatis-plus` config keys

## 12. rinko-notify — Verification

- [x] 12.1 `mvn compile -pl rinko-notify -am` succeeds

## 13. rinko-oss — Dependencies

- [x] 13.1 Replace `org.mybatis.spring.boot:mybatis-spring-boot-starter` with `com.baomidou:mybatis-plus-spring-boot4-starter` in `rinko-oss/pom.xml`
- [x] 13.2 Remove `org.springframework.boot:spring-boot-starter-data-jdbc` from `rinko-oss/pom.xml`

## 14. rinko-oss — Entity Annotations

- [x] 14.1 `FileMetadata.java`: Replace `@Table` → `@TableName("file_metadata")`, `@Id` → `@TableId(type = IdType.INPUT)`
- [x] 14.2 `FileVersion.java`: Replace `@Table` → `@TableName("file_versions")`, `@Id` → `@TableId(type = IdType.INPUT)`
- [x] 14.3 `VideoResolutionEntity.java`: Replace `@Table` → `@TableName("video_resolutions")`, `@Id` → `@TableId(type = IdType.INPUT)`

## 15. rinko-oss — Configuration

- [x] 15.1 Update `application.yml`: `mybatis` → `mybatis-plus` config keys
- [x] 15.2 Keep existing `pagehelper` config (already present)

## 16. rinko-oss — Verification

- [x] 16.1 `mvn compile -pl rinko-oss -am` succeeds

## 17. rinko-scheduler — Dependencies

- [x] 17.1 Replace `org.mybatis.spring.boot:mybatis-spring-boot-starter` with `com.baomidou:mybatis-plus-spring-boot4-starter` in `rinko-scheduler/pom.xml`
- [x] 17.2 Remove `org.springframework.boot:spring-boot-starter-data-jdbc` from `rinko-scheduler/pom.xml`
- [x] 17.3 Add `com.github.pagehelper:pagehelper-spring-boot-starter` to `rinko-scheduler/pom.xml`

## 18. rinko-scheduler — Entity Annotations

- [x] 18.1 `SchedulerJob.java`: Replace `@Table` → `@TableName("scheduler_jobs")`, `@Id` → `@TableId(type = IdType.INPUT)`
- [x] 18.2 `SchedulerExecution.java`: Replace `@Table` → `@TableName("scheduler_executions")`, `@Id` → `@TableId(type = IdType.INPUT)`
- [x] 18.3 `SchedulerDependency.java`: Replace `@Table` → `@TableName("scheduler_dependencies")`, `@Id` → `@TableId(type = IdType.INPUT)`

## 19. rinko-scheduler — Configuration

- [x] 19.1 Update `application.yml`: `mybatis` → `mybatis-plus` config keys; add `pagehelper` config

## 20. rinko-scheduler — Verification

- [x] 20.1 `mvn compile -pl rinko-scheduler -am` succeeds
