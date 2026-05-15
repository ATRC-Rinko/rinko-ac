package com.rinko.oss.media;

import com.rinko.oss.config.OssProperties;
import com.rinko.oss.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ImageProcessor {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessor.class);
    private final StorageService storageService;
    private final OssProperties ossProperties;

    public ImageProcessor(StorageService storageService, OssProperties ossProperties) {
        this.storageService = storageService;
        this.ossProperties = ossProperties;
    }

    public void generateThumbnail(InputStream originalStream, String baseKey) {
        try {
            BufferedImage original = ImageIO.read(originalStream);
            if (original == null) return;

            int origW = original.getWidth();
            int origH = original.getHeight();

            for (OssProperties.Thumbnail.Resolution res : ossProperties.getThumbnail().getResolutions()) {
                // 保持宽高比：按长边计算缩放比例
                double scale = (double) res.getMaxWidth() / Math.max(origW, origH);
                // 不放大（原图小于目标分辨率时保持原尺寸）
                scale = Math.min(scale, 1.0);

                int newW = (int) (origW * scale);
                int newH = (int) (origH * scale);

                BufferedImage thumb = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = thumb.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(original, 0, 0, newW, newH, null);
                g.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(thumb, "jpg", baos);
                byte[] thumbBytes = baos.toByteArray();

                String thumbKey = baseKey + "/" + res.getLabel() + ".jpg";
                storageService.store(new ByteArrayInputStream(thumbBytes), thumbKey, thumbBytes.length, "image/jpeg");
                log.info("Generated thumbnail {} ({}x{})", thumbKey, newW, newH);
            }
        } catch (IOException e) {
            log.warn("Failed to generate thumbnail for {}: {}", baseKey, e.getMessage());
        }
    }
}
