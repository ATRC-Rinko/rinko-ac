package com.rinko.oss.service;

import com.rinko.oss.config.OssProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@ConditionalOnProperty(name = "rinko.oss.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);
    private final Path basePath;

    public LocalStorageService(OssProperties ossProperties) {
        this.basePath = Paths.get(ossProperties.getLocal().getBasePath()).toAbsolutePath();
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + basePath, e);
        }
    }

    @Override
    public void store(InputStream inputStream, String key, long contentLength, String contentType) {
        Path filePath = basePath.resolve(key);
        try {
            Files.createDirectories(filePath.getParent());
            try (OutputStream os = Files.newOutputStream(filePath)) {
                inputStream.transferTo(os);
            }
            log.debug("Stored local file: {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + key, e);
        }
    }

    @Override
    public InputStream getInputStream(String key) {
        try {
            return Files.newInputStream(basePath.resolve(key));
        } catch (IOException e) {
            throw new RuntimeException("File not found: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(basePath.resolve(key));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", key, e);
        }
    }

    @Override
    public String generatePresignUrl(String key, int expiresSeconds) {
        return "/api/v1/oss/download/by-key?key=" + key;
    }

    @Override
    public String initiateMultipartUpload(String key, String contentType) {
        String uploadId = UUID.randomUUID().toString();
        Path partsDir = basePath.resolve(key + ".parts").resolve(uploadId);
        try {
            Files.createDirectories(partsDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create parts dir: " + partsDir, e);
        }
        return uploadId;
    }

    @Override
    public String uploadPart(String key, String uploadId, int partNumber, InputStream data, long size) {
        Path partFile = basePath.resolve(key + ".parts").resolve(uploadId).resolve("part-" + partNumber);
        try {
            Files.createDirectories(partFile.getParent());
            try (OutputStream os = Files.newOutputStream(partFile)) {
                data.transferTo(os);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload part " + partNumber, e);
        }
        return "etag-" + partNumber;
    }

    @Override
    public void completeMultipartUpload(String key, String uploadId, List<PartETag> parts) {
        Path partsDir = basePath.resolve(key + ".parts").resolve(uploadId);
        Path finalFile = basePath.resolve(key);
        try {
            Files.createDirectories(finalFile.getParent());
            try (OutputStream os = Files.newOutputStream(finalFile)) {
                for (PartETag part : parts.stream().sorted(Comparator.comparingInt(PartETag::partNumber)).toList()) {
                    Path partFile = partsDir.resolve("part-" + part.partNumber());
                    Files.copy(partFile, os);
                }
            }
            deleteDirectory(partsDir.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to complete multipart upload: " + key, e);
        }
    }

    @Override
    public void abortMultipartUpload(String key, String uploadId) {
        Path partsDir = basePath.resolve(key + ".parts").resolve(uploadId);
        try {
            deleteDirectory(partsDir.getParent());
        } catch (IOException e) {
            log.warn("Failed to abort multipart upload: {}", key, e);
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (var files = Files.walk(dir)) {
                files.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
        }
    }
}
