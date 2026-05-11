## Context

`rinko-oss` 是对象存储服务，对接 rustfs（S3 兼容存储）。技术栈：Servlet + Jetty + JDBC。

## Goals / Non-Goals

**Goals:**
- S3 兼容存储 + 本地存储，配置切换
- 文件元数据 + 版本历史 + 视频分辨率元数据持久化
- REST API：上传（流式）、下载、预签名 URL、列表、删除、目录、版本、媒体处理
- 分片上传（>5MB）
- 图片自动缩略图（200x200，可配置）
- **视频多分辨率转码**（480p/720p/1080p，FFmpeg 命令行）
- 流式上传避免全量加载内存

**Non-Goals:**
- 不实现 S3 SSE 服务端加密
- 不实现实时视频流（HLS/DASH）

## Decisions

### 1. 流式上传

`PutObjectRequest` + `RequestBody.fromInputStream()` — 文件内容不经过内存全量加载。

### 2. 版本管理

S3 Key: `{baseDir}/{fileId}/v{version}/{originalName}`。`file_versions` 表记录每版本元数据。

### 3. 目录结构

`parentId` 自引用树形目录。文件夹是 `file_size = 0` 的记录。

### 4. 图片缩略图

上传 `image/*` 文件时自动生成。Java `BufferedImage` + `ImageIO`，缩略图 key: `{baseDir}/{fileId}/thumb.jpg`。

### 5. 视频多分辨率转码

**决策**: 上传 `video/*` 文件时异步生成多分辨率版本。使用 Java `ProcessBuilder` 调用系统 `ffmpeg` 命令行。

**分辨率配置**（可配置 `rinko.oss.video.resolutions`）:
| 分辨率 | 宽度 | 高度 | 码率 | 标签 |
|--------|------|------|------|------|
| 480p | 854 | 480 | 1Mbps | `480p` |
| 720p | 1280 | 720 | 2.5Mbps | `720p` |
| 1080p | 1920 | 1080 | 5Mbps | `1080p` |

原视频分辨率 ≤ 目标分辨率时跳过该档转码。

**存储**: 每档分辨率存储为独立 S3 对象：`{baseDir}/{fileId}/v{version}/{resolution}.mp4`

**元数据**: `video_resolutions` 表记录每档分辨率的状态（PENDING/PROCESSING/COMPLETED/FAILED）、文件大小、存储路径。

**异步处理**: 上传完成后立即返回 201，转码任务提交到 `ExecutorService` 后台执行。客户端通过 `GET /api/v1/oss/video/{fileId}/resolutions` 查询转码进度。

#### Scenario: Upload 4K video

- **WHEN** a 3840x2160 MP4 file is uploaded
- **THEN** HTTP 201 SHALL return immediately with resolution status all PENDING
- **AND** 3 resolution versions SHALL be queued: 480p, 720p, 1080p
- **AND** poll `GET /video/{id}/resolutions` SHALL show COMPLETED for each finished resolution

#### Scenario: FFmpeg unavailable

- **WHEN** FFmpeg is not installed
- **THEN** video upload SHALL still succeed (original file stored)
- **AND** `video_resolutions` entries SHALL all be FAILED with error "ffmpeg not found"
