package com.rinko.oss.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rinko.infra.dto.PageResponse;
import com.rinko.infra.exception.InternalException;
import com.rinko.infra.exception.NotFoundException;
import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.oss.config.OssProperties;
import com.rinko.oss.model.entity.FileMetadata;
import com.rinko.oss.model.entity.FileVersion;
import com.rinko.oss.model.entity.VideoResolutionEntity;
import com.rinko.oss.media.MediaProcessingService;
import com.rinko.oss.repository.FileMetadataMapper;
import com.rinko.oss.repository.FileVersionRepository;
import com.rinko.oss.repository.VideoResolutionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private final StorageService storageService;
    private final FileMetadataMapper fileMetadataMapper;
    private final FileVersionRepository fileVersionRepository;
    private final VideoResolutionRepository videoResolutionRepository;
    private final MediaProcessingService mediaProcessingService;
    private final OssProperties ossProperties;
    private final SnowflakeIdGenerator idGenerator;
    private final Map<String, MultipartSession> multipartSessions = new ConcurrentHashMap<>();

    @Transactional
    public FileMetadata upload(InputStream inputStream, String originalName, String contentType, Long parentId) throws IOException {
        byte[] bytes = readAllBytes(inputStream);
        long fileId = idGenerator.nextId();
        String sha256 = sha256(bytes);
        String key = fileId + "/v1/" + originalName;

        storageService.store(new ByteArrayInputStream(bytes), key, bytes.length, contentType);

        FileMetadata meta = new FileMetadata();
        meta.setId(fileId);
        meta.setOriginalName(originalName);
        meta.setFileSize(bytes.length);
        meta.setContentType(contentType);
        meta.setSha256(sha256);
        meta.setStoragePath(key);
        meta.setStorageType(ossProperties.getStorageType());
        meta.setParentId(parentId);
        meta.setDirectory(false);
        meta.setCurrentVersion(1);
        fileMetadataMapper.insert(meta);

        FileVersion version = new FileVersion();
        version.setId(idGenerator.nextId());
        version.setFileId(fileId);
        version.setVersion(1);
        version.setFileSize(bytes.length);
        version.setSha256(sha256);
        version.setStoragePath(key);
        fileVersionRepository.insert(version);

        mediaProcessingService.processMedia(fileId, 1, contentType, new ByteArrayInputStream(bytes), key.replace("/" + originalName, ""));

        return meta;
    }

    public FileMetadata getMetadata(long fileId) {
        FileMetadata meta = fileMetadataMapper.selectById(fileId);
        if (meta == null) {
            throw new NotFoundException("File not found: " + fileId);
        }
        return meta;
    }

    public InputStream download(long fileId) {
        FileMetadata meta = getMetadata(fileId);
        return storageService.getInputStream(meta.getStoragePath());
    }

    public String presignUrl(long fileId, int expires) {
        FileMetadata meta = getMetadata(fileId);
        return storageService.generatePresignUrl(meta.getStoragePath(), expires);
    }

    @Transactional
    public void delete(long fileId) {
        FileMetadata meta = getMetadata(fileId);
        storageService.delete(meta.getStoragePath());
        fileMetadataMapper.deleteById(fileId);
    }

    public PageResponse<FileMetadata> listFiles(Long parentId, int page, int size) {
        var wrapper = new LambdaQueryWrapper<FileMetadata>()
                .eq(FileMetadata::isDirectory, false)
                .orderByDesc(FileMetadata::getCreatedAt);
        if (parentId != null) {
            wrapper.eq(FileMetadata::getParentId, parentId);
        }
        Page<FileMetadata> result = fileMetadataMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public FileMetadata createDirectory(String name, Long parentId) {
        FileMetadata dir = new FileMetadata();
        dir.setId(idGenerator.nextId());
        dir.setOriginalName(name);
        dir.setFileSize(0);
        dir.setStoragePath("");
        dir.setStorageType(ossProperties.getStorageType());
        dir.setParentId(parentId);
        dir.setDirectory(true);
        fileMetadataMapper.insert(dir);
        return dir;
    }

    public List<FileVersion> listVersions(long fileId) {
        return fileVersionRepository.selectList(
                new LambdaQueryWrapper<FileVersion>()
                        .eq(FileVersion::getFileId, fileId)
                        .orderByDesc(FileVersion::getVersion));
    }

    @Transactional
    public void rollback(long fileId, int targetVersion) {
        FileMetadata meta = getMetadata(fileId);
        FileVersion target = fileVersionRepository.selectOne(
                new LambdaQueryWrapper<FileVersion>()
                        .eq(FileVersion::getFileId, fileId)
                        .eq(FileVersion::getVersion, targetVersion));
        if (target == null) {
            throw new NotFoundException("Version not found: " + targetVersion);
        }
        meta.setStoragePath(target.getStoragePath());
        meta.setCurrentVersion(targetVersion);
        meta.setSha256(target.getSha256());
        meta.setFileSize(target.getFileSize());
        fileMetadataMapper.updateById(meta);
    }

    public InputStream downloadByKey(String key) {
        return storageService.getInputStream(key);
    }

    public List<VideoResolutionEntity> listVideoResolutions(long fileId) {
        FileMetadata meta = getMetadata(fileId);
        return videoResolutionRepository.selectList(
                new LambdaQueryWrapper<VideoResolutionEntity>()
                        .eq(VideoResolutionEntity::getFileId, fileId)
                        .eq(VideoResolutionEntity::getVersion, meta.getCurrentVersion())
                        .orderByAsc(VideoResolutionEntity::getResolution));
    }

    public MultipartSession initiateMultipartUpload(String originalName, String contentType, Long parentId) {
        long fileId = idGenerator.nextId();
        String key = fileId + "/v1/" + originalName;
        String uploadId = storageService.initiateMultipartUpload(key, contentType);
        MultipartSession session = new MultipartSession(uploadId, fileId, key, originalName, contentType, parentId);
        multipartSessions.put(uploadId, session);
        return session;
    }

    public StorageService.PartETag uploadPart(String uploadId, int partNumber, InputStream data, long size) {
        MultipartSession session = multipartSessions.get(uploadId);
        if (session == null) {
            throw new NotFoundException("Upload session not found: " + uploadId);
        }
        return new StorageService.PartETag(partNumber,
                storageService.uploadPart(session.key, uploadId, partNumber, data, size));
    }

    @Transactional
    public FileMetadata completeMultipartUpload(String uploadId, List<StorageService.PartETag> parts) {
        MultipartSession session = multipartSessions.remove(uploadId);
        if (session == null) {
            throw new NotFoundException("Upload session not found: " + uploadId);
        }
        storageService.completeMultipartUpload(session.key, uploadId, parts);

        FileMetadata meta = new FileMetadata();
        meta.setId(session.fileId);
        meta.setOriginalName(session.originalName);
        meta.setFileSize(0);
        meta.setContentType(session.contentType);
        meta.setStoragePath(session.key);
        meta.setStorageType(ossProperties.getStorageType());
        meta.setParentId(session.parentId);
        meta.setDirectory(false);
        meta.setCurrentVersion(1);
        fileMetadataMapper.insert(meta);

        return meta;
    }

    public void abortMultipartUpload(String uploadId) {
        MultipartSession session = multipartSessions.remove(uploadId);
        if (session == null) {
            return;
        }
        storageService.abortMultipartUpload(session.key, uploadId);
    }

    public record MultipartSession(String uploadId, long fileId, String key, String originalName, String contentType, Long parentId) {}

    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int n;
        while ((n = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }

    private String sha256(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalException("SHA-256 algorithm not available", e);
        }
    }
}
