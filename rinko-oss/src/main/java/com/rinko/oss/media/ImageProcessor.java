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

            int w = ossProperties.getThumbnail().getWidth();
            int h = ossProperties.getThumbnail().getHeight();
            BufferedImage thumb = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumb.createGraphics();
            g.drawImage(original.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumb, "jpg", baos);
            byte[] thumbBytes = baos.toByteArray();

            String thumbKey = baseKey + "/thumb.jpg";
            storageService.store(new ByteArrayInputStream(thumbBytes), thumbKey, thumbBytes.length, "image/jpeg");
            log.info("Generated thumbnail: {}", thumbKey);
        } catch (IOException e) {
            log.warn("Failed to generate thumbnail for {}: {}", baseKey, e.getMessage());
        }
    }
}
