package com.rinko.oss.media;

import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MediaProcessingService {

    private final ImageProcessor imageProcessor;
    private final VideoProcessor videoProcessor;

    public MediaProcessingService(ImageProcessor imageProcessor, VideoProcessor videoProcessor) {
        this.imageProcessor = imageProcessor;
        this.videoProcessor = videoProcessor;
    }

    public void processMedia(long fileId, int version, String contentType, InputStream stream, String baseKey) {
        if (contentType == null) return;

        if (contentType.startsWith("image/")) {
            imageProcessor.generateThumbnail(stream, baseKey);
        } else if (contentType.startsWith("video/")) {
            videoProcessor.transcode(fileId, version, stream, baseKey);
        }
    }
}
