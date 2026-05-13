package com.rinko.oss.media;

import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.oss.config.OssProperties;
import com.rinko.oss.entity.VideoResolutionEntity;
import com.rinko.oss.repository.VideoResolutionRepository;
import com.rinko.oss.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class VideoProcessor {

    private static final Logger log = LoggerFactory.getLogger(VideoProcessor.class);
    private final StorageService storageService;
    private final OssProperties ossProperties;
    private final VideoResolutionRepository videoResolutionRepository;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public VideoProcessor(StorageService storageService, OssProperties ossProperties,
                           VideoResolutionRepository videoResolutionRepository) {
        this.storageService = storageService;
        this.ossProperties = ossProperties;
        this.videoResolutionRepository = videoResolutionRepository;
    }

    @Async
    public void transcode(long fileId, int version, InputStream originalStream, String baseKey) {
        Path tempDir = null;
        Path originalFile = null;
        try {
            tempDir = Files.createTempDirectory("video-transcode-");
            originalFile = tempDir.resolve("original.mp4");
            Files.copy(originalStream, originalFile);

            for (OssProperties.Video.Resolution res : ossProperties.getVideo().getResolutions()) {
                var entity = new VideoResolutionEntity();
                entity.setId(idGenerator.nextId());
                entity.setFileId(fileId);
                entity.setVersion(version);
                entity.setResolution(res.getLabel());
                entity.setStatus("PROCESSING");
                videoResolutionRepository.insert(entity);

                Path outputFile = tempDir.resolve(res.getLabel() + ".mp4");
                try {
                    String[] cmd = {
                            "ffmpeg", "-y", "-i", originalFile.toString(),
                            "-vf", "scale=" + res.getWidth() + ":" + res.getHeight(),
                            "-b:v", res.getBitrate(),
                            "-c:v", "libx264", "-c:a", "aac",
                            outputFile.toString()
                    };
                    Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
                    int exitCode = process.waitFor();

                    if (exitCode == 0 && Files.exists(outputFile) && Files.size(outputFile) > 0) {
                        String key = baseKey + "/" + res.getLabel() + ".mp4";
                        long fileSize = Files.size(outputFile);
                        try (InputStream is = Files.newInputStream(outputFile)) {
                            storageService.store(is, key, fileSize, "video/mp4");
                        }
                        entity.setStatus("COMPLETED");
                        entity.setFileSize(fileSize);
                        entity.setStoragePath(key);
                    } else {
                        entity.setStatus("FAILED");
                        entity.setErrorMessage("ffmpeg exit code: " + exitCode);
                    }
                } catch (IOException | InterruptedException e) {
                    entity.setStatus("FAILED");
                    entity.setErrorMessage(e.getMessage());
                }
                videoResolutionRepository.updateById(entity);
            }
        } catch (IOException e) {
            log.error("Video transcode failed for fileId={}", fileId, e);
        } finally {
            if (originalFile != null) {
                try { Files.deleteIfExists(originalFile); } catch (IOException ignored) {}
            }
            if (tempDir != null) {
                try { Files.deleteIfExists(tempDir); } catch (IOException ignored) {}
            }
        }
    }
}
