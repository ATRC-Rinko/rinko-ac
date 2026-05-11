## Why

`rinko-oss` 是对象存储服务，负责文件上传/下载、分片上传、预签名 URL、目录管理、版本控制和媒体处理（图片缩略图 + 视频多分辨率转码）。根据架构设计对接 rustfs（兼容 AWS S3 API），同时支持本地存储作为开发环境降级方案。当前模块完全是骨架状态，需要实现核心存储功能。

## What Changes

- Maven 依赖：AWS SDK S3、Spring Data JDBC
- 文件上传（流式 `RequestBody.fromInputStream()`）
- 文件下载、预签名 URL
- 目录/文件夹结构（`parentId` 树形）
- 文件版本管理（历史版本、回滚）
- 图片缩略图自动生成（`BufferedImage` + ImageIO）
- **视频多分辨率转码**：上传视频文件时自动生成多分辨率版本（480p/720p/1080p），通过 FFmpeg 命令行转码，各分辨率版本存储为独立 S3 对象
- Flyway 迁移：`file_metadata` + `file_versions` + `video_resolutions` 表

## Capabilities

### New Capabilities

- `oss-upload`: 流式文件上传、分片上传
- `oss-download`: 代理下载、预签名 URL
- `oss-metadata`: 元数据管理、分页、删除、目录结构
- `oss-versioning`: 文件版本管理 — 历史版本列表、版本回滚
- `oss-media-processing`: 媒体处理 — 图片缩略图 + 视频多分辨率转码

### Modified Capabilities

<!-- 无需修改已有 capability -->

## Impact

- 新增依赖：`spring-boot-starter-data-jdbc`、`software.amazon.awssdk:s3`
- 外部依赖：FFmpeg（运行环境需安装 `ffmpeg` 命令）
- 新增文件：~22 个 Java 文件
- Flyway 迁移：3 个 SQL 文件
