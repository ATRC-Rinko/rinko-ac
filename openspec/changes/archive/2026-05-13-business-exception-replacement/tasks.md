## 1. rinko-infra — SortOrder.java

- [x] 1.1 `IllegalArgumentException("Invalid sort direction...")` → `ValidationException("Invalid sort direction...")`，添加 import

## 2. rinko-infra — SnowflakeIdGenerator.java

- [x] 2.1 `IllegalStateException("Clock moved backwards...")` → `InternalException("Clock moved backwards...")`，添加 import

## 3. rinko-oss — FileService.java

- [x] 3.1 `RuntimeException("SHA-256 algorithm not available")` → `InternalException(...)`，更新 import

## 4. rinko-oss — LocalStorageService.java

- [x] 4.1 构造器 `RuntimeException` → `InternalException`（目录创建失败）
- [x] 4.2 `store()` `RuntimeException` → `InternalException`（文件存储失败）
- [x] 4.3 `getInputStream()` `RuntimeException` → `NotFoundException`（文件不存在）
- [x] 4.4 `initiateMultipartUpload()` `RuntimeException` → `InternalException`
- [x] 4.5 `uploadPart()` `RuntimeException` → `InternalException`
- [x] 4.6 `completeMultipartUpload()` `RuntimeException` → `InternalException`
- [x] 4.7 更新 import：移除 `RuntimeException`（如无其他使用），添加对应的 `RinkoException` 子类

## 5. rinko-scheduler — SchedulerService.java

- [x] 5.1 `triggerJob()` `RuntimeException` → `InternalException`
- [x] 5.2 `pauseJob()` `RuntimeException` → `InternalException`
- [x] 5.3 `resumeJob()` `RuntimeException` → `InternalException`
- [x] 5.4 `scheduleQuartzJob()` `RuntimeException` → `InternalException`
- [x] 5.5 更新 import

## 6. rinko-scheduler — Job Executors

- [x] 6.1 `ShellJobExecutor.java` `RuntimeException` → `InternalException`，更新 import
- [x] 6.2 `BeanJobExecutor.java` `RuntimeException` → `InternalException`，更新 import
- [x] 6.3 `HttpJobExecutor.java` `RuntimeException` → `InternalException`，更新 import

## 7. 构建验证

- [x] 7.1 `mvn compile -pl rinko-infra,rinko-oss,rinko-scheduler -am -DskipTests` 成功
