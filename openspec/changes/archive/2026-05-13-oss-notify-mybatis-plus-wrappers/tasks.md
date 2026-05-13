## 1. rinko-oss — FileMetadataMapper

- [x] 1.1 `FileMetadataMapper` extends `BaseMapper<FileMetadata>`，删除 `findById`/`insert`/`update`/`deleteById`/`findByParentId`（BaseMapper 覆盖 + 移除未使用方法）
- [x] 1.2 更新 `FileService.java`：`findById(id)` → `selectById(id)`；`insert(meta)` → `insert(meta)`；`update(meta)` → `updateById(meta)`；`deleteById(id)` → `deleteById(id)`
- [x] 1.3 `findFilesByParentId` / `findAllFiles` / `countByParentId` → 在 `FileService` 中使用 `LambdaQueryWrapper` + `selectList()` / `selectCount()`

## 2. rinko-oss — FileVersionRepository

- [x] 2.1 `FileVersionRepository` extends `BaseMapper<FileVersion>`，删除 `insert`/`getMaxVersion`（BaseMapper 覆盖 + 移除未使用方法）
- [x] 2.2 删除 `findByFileIdAndVersion`，在 `FileService.rollback()` 中使用 `LambdaQueryWrapper` + `selectOne()`
- [x] 2.3 删除 `findByFileIdOrderByVersionDesc`，在 `FileService.listVersions()` 中使用 `LambdaQueryWrapper` + `selectList()`

## 3. rinko-oss — VideoResolutionRepository

- [x] 3.1 `VideoResolutionRepository` extends `BaseMapper<VideoResolutionEntity>`，删除 `insert`/`update`（BaseMapper 覆盖）
- [x] 3.2 删除 `findByFileIdAndVersionOrderByResolution`，在 `FileService.listVideoResolutions()` 和 `VideoProcessor.transcode()` 中使用 `LambdaQueryWrapper` + `selectList()`

## 4. rinko-oss — XML 清理

- [x] 4.1 删除 `FileMetadataMapper.xml`
- [x] 4.2 删除 `FileVersionMapper.xml`
- [x] 4.3 删除 `VideoResolutionMapper.xml`

## 5. rinko-notify — NotificationTemplateMapper

- [x] 5.1 `NotificationTemplateMapper` extends `BaseMapper<NotificationTemplate>`，删除 `insert`/`update`/`deleteById`/`findById`（BaseMapper 覆盖）
- [x] 5.2 删除 `findAll`，在 `TemplateController` 中使用 `LambdaQueryWrapper` + `selectList()`
- [x] 5.3 删除 `findByCode`，在 `NotifyService` 和 `TemplateController` 更新时使用 `LambdaQueryWrapper` + `selectOne()`
- [x] 5.4 更新 `TemplateController.java`：所有调用改为 `BaseMapper` 方法 + `LambdaQueryWrapper`

## 6. rinko-notify — NotificationHistoryMapper

- [x] 6.1 `NotificationHistoryMapper` extends `BaseMapper<NotificationHistory>`，删除 `insert`/`findById`（BaseMapper 覆盖）；保留 `batchInsert` 方法签名
- [x] 6.2 删除 `updateStatus`，在 `EmailChannel`/`SmsChannel` 中使用 `LambdaUpdateWrapper` + `update()`
- [x] 6.3 删除 `markRead`，在 `NotifyService.markRead()` 中使用 `LambdaUpdateWrapper` + `update()`
- [x] 6.4 删除 `findByRecipientAndChannel`，在 `NotifyService.getInbox()` 中使用 `LambdaQueryWrapper` + `selectList()`
- [x] 6.5 删除 `countUnread`，在 `NotifyService.getUnreadCount()` 中使用 `LambdaQueryWrapper` + `selectCount()`

## 7. rinko-notify — Channel 更新

- [x] 7.1 `EmailChannel.java`：`insert(history)` → `insert(history)`（BaseMapper）；`updateStatus` → `LambdaUpdateWrapper`
- [x] 7.2 `SmsChannel.java`：同上
- [x] 7.3 `InAppChannel.java`：`insert(history)` → `insert(history)`（BaseMapper）

## 8. rinko-notify — Service + XML 清理

- [x] 8.1 更新 `NotifyService.java`：`findByCode` → `LambdaQueryWrapper`；`findByRecipientAndChannel` / `countUnread` / `markRead` → Wrapper
- [x] 8.2 缩编 `NotificationHistoryMapper.xml` — 仅保留 `batchInsert` SQL
- [x] 8.3 删除 `NotificationTemplateMapper.xml`

## 9. 构建验证

- [x] 9.1 `mvn compile -pl rinko-oss -am -DskipTests` 成功
- [x] 9.2 `mvn compile -pl rinko-notify -am -DskipTests` 成功
