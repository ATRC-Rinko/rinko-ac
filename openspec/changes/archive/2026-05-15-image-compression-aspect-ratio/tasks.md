## 1. OssProperties.Thumbnail

- [x] 1.1 `width`/`height` → `List<Resolution>`（`label` + `maxWidth`），默认 small/medium/large

## 2. ImageProcessor

- [x] 2.1 保持宽高比：`scale = maxWidth / max(origW, origH)`，`scale = min(scale, 1.0)`
- [x] 2.2 多分辨率输出：`{baseKey}/{label}.jpg`
- [x] 2.3 双线性插值渲染

## 3. MediaController

- [x] 3.1 `thumbnail` 接口新增 `?label=` 参数（默认 small）

## 4. 配置

- [x] 4.1 `rinko-oss-dev.yml` 配置格式更新

## 5. 构建验证

- [x] 5.1 `mvn compile -pl rinko-oss -am` 成功
