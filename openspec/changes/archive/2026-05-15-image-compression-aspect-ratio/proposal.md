## Why

当前缩略图只有固定 200x200 一种尺寸，且 `getScaledInstance(w, h)` 直接拉伸不保持宽高比。需要改为保持宽高比的等比例压缩，并支持多种分辨率输出。

## What Changes

- `OssProperties.Thumbnail`：`width`/`height` → `List<Resolution>`，每个 resolution 含 `label` + `maxWidth`
- `ImageProcessor`：保持宽高比压缩，输出多种分辨率
- `rinko-oss-dev.yml`：更新配置

## Impact

| 文件 | 操作 |
|------|------|
| `OssProperties.java` | Thumbnail 改用 Resolution 列表 |
| `ImageProcessor.java` | 保持宽高比 + 多分辨率输出 |
| `rinko-oss-dev.yml` | 配置格式更新 |
