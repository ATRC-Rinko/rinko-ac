## Decisions

1. **保持宽高比**: 计算 `scale = maxWidth / max(origW, origH)`，缩放后 `newW = origW * scale`, `newH = origH * scale`
2. **多分辨率**: `List<Resolution>` 循环处理，每个 resolution 输出一个文件：`{baseKey}/{label}.jpg`
3. **向下缩放**: 原图小于目标分辨率时不放大（`scale = min(scale, 1.0)`）
4. **JPEG 输出**: 保持 ImageIO JPEG 格式
