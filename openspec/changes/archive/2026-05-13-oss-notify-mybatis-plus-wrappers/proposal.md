## Why

rinko-oss 和 rinko-notify 在上次迁移中已将 starter 替换为 MyBatis-Plus，但 Mapper 接口仍使用纯 MyBatis XML 方式定义 SQL，未利用 `BaseMapper<T>` 内建 CRUD 和 `LambdaQueryWrapper` 类型安全查询。本次变更将 Mapper 接口改为继承 `BaseMapper`，用 `LambdaQueryWrapper`/`LambdaUpdateWrapper` 替代 XML 中的 SELECT/UPDATE/DELETE 语句，消除手写 XML 样板代码，获得编译期列名校验和 IDE 重构支持。

## What Changes

### rinko-oss
- `FileMetadataMapper` 继承 `BaseMapper<FileMetadata>`，移除 `findById`/`insert`/`update`/`deleteById`（BaseMapper 已提供）；移除未使用的方法 `findByParentId`
- `findFilesByParentId` / `findAllFiles` / `countByParentId` → Service 层使用 `LambdaQueryWrapper` 调用 `selectList()` / `selectCount()`
- `FileVersionRepository` 继承 `BaseMapper<FileVersion>`，移除 `insert`/`findByFileIdAndVersion`（BaseMapper 提供 `insert`/`selectOne`）；移除未使用的 `getMaxVersion`
- `VideoResolutionRepository` 继承 `BaseMapper<VideoResolutionEntity>`，移除 `insert`/`update`
- 删除 3 个 XML Mapper 文件（FileMetadataMapper.xml / FileVersionMapper.xml / VideoResolutionMapper.xml）
- 更新 `FileService.java` / `VideoProcessor.java` 使用 Wrapper 查询

### rinko-notify
- `NotificationHistoryMapper` 继承 `BaseMapper<NotificationHistory>`，移除 `insert`/`findById`（BaseMapper 提供）；保留 `batchInsert` 自定义方法（批量 insert 需要 XML）
- `updateStatus` / `markRead` → 使用 `LambdaUpdateWrapper` 做部分字段更新
- `findByRecipientAndChannel` / `countUnread` → 使用 `LambdaQueryWrapper` 调用 `selectList()` / `selectCount()`
- `NotificationTemplateMapper` 继承 `BaseMapper<NotificationTemplate>`，移除 `insert`/`update`/`deleteById`/`findById`（BaseMapper 提供）
- `findAll` / `findByCode` → 使用 `LambdaQueryWrapper` 调用 `selectList()` / `selectOne()`
- 删除 NotificationTemplateMapper.xml；缩编 NotificationHistoryMapper.xml（仅保留 `batchInsert`）
- 更新 `TemplateController.java` / `NotifyService.java` / `EmailChannel.java` / `SmsChannel.java` / `InAppChannel.java`

## Capabilities

### New Capabilities

None. Pure implementation change — no API or behavioral changes.

### Modified Capabilities

None. All existing spec requirements unchanged.

## Impact

**涉及文件（rinko-oss，5 文件 + 删除 3 XML）：**
| 文件 | 操作 |
|------|------|
| `FileMetadataMapper.java` | 继承 `BaseMapper`，精简方法 |
| `FileVersionRepository.java` | 继承 `BaseMapper`，精简方法 |
| `VideoResolutionRepository.java` | 继承 `BaseMapper`，精简方法 |
| `FileService.java` | 使用 `LambdaQueryWrapper` / `selectList` / `selectCount` |
| `VideoProcessor.java` | 使用 `LambdaQueryWrapper` / `selectList` |
| `FileMetadataMapper.xml` | 删除 |
| `FileVersionMapper.xml` | 删除 |
| `VideoResolutionMapper.xml` | 删除 |

**涉及文件（rinko-notify，7 文件 + 修改 1 XML + 删除 1 XML）：**
| 文件 | 操作 |
|------|------|
| `NotificationHistoryMapper.java` | 继承 `BaseMapper`，精简方法，保留 `batchInsert` |
| `NotificationTemplateMapper.java` | 继承 `BaseMapper`，精简方法 |
| `NotifyService.java` | 使用 `LambdaQueryWrapper` |
| `TemplateController.java` | 使用 `BaseMapper` 方法 + `LambdaQueryWrapper` |
| `EmailChannel.java` | 使用 `LambdaUpdateWrapper` |
| `SmsChannel.java` | 使用 `LambdaUpdateWrapper` |
| `InAppChannel.java` | 使用 `BaseMapper.insert()` |
| `NotificationHistoryMapper.xml` | 缩编，仅保留 `batchInsert` |
| `NotificationTemplateMapper.xml` | 删除 |
