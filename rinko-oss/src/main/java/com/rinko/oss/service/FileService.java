package com.rinko.oss.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rinko.infra.dto.PageResponse;
import com.rinko.infra.exception.NotFoundException;
import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.oss.config.OssProperties;
import com.rinko.oss.entity.FileMetadata;
import com.rinko.oss.entity.FileVersion;
import com.rinko.oss.entity.VideoResolutionEntity;
import com.rinko.oss.media.MediaProcessingService;
import com.rinko.oss.repository.FileMetadataMapper;
import com.rinko.oss.repository.FileVersionRepository;
import com.rinko.oss.repository.VideoResolutionRepository;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private final StorageService storageService;
    private final FileMetadataMapper fileMetadataMapper;
    private final FileVersionRepository fileVersionRepository;
    private final VideoResolutionRepository videoResolutionRepository;
    private final MediaProcessingService mediaProcessingService;
    private final OssProperties ossProperties;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();
    private final Map<String, MultipartSession> multipartSessions = new ConcurrentHashMap<>();

    public FileService(StorageService storageService, FileMetadataMapper fileMetadataMapper,
                        FileVersionRepository fileVersionRepository,
                        VideoResolutionRepository videoResolutionRepository,
                        MediaProcessingService mediaProcessingService, OssProperties ossProperties) {
        this.storageService = storageService;
        this.fileMetadataMapper = fileMetadataMapper;
        this.fileVersionRepository = fileVersionRepository;
        this.videoResolutionRepository = videoResolutionRepository;
        this.mediaProcessingService = mediaProcessingService;
        this.ossProperties = ossProperties;
    }

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
        return fileMetadataMapper.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));
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
        PageHelper.startPage(page, size);
        List<FileMetadata> list = parentId != null
                ? fileMetadataMapper.findFilesByParentId(parentId)
                : fileMetadataMapper.findAllFiles();
        PageInfo<FileMetadata> pageInfo = new PageInfo<>(list);
        return new PageResponse<>(pageInfo.getList(), pageInfo.getTotal(), page, size);
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
        return fileVersionRepository.findByFileIdOrderByVersionDesc(fileId);
    }

    @Transactional
    public void rollback(long fileId, int targetVersion) {
        FileMetadata meta = getMetadata(fileId);
        FileVersion target = fileVersionRepository.findByFileIdAndVersion(fileId, targetVersion)
                .orElseThrow(() -> new NotFoundException("Version not found: " + targetVersion));
        meta.setStoragePath(target.getStoragePath());
        meta.setCurrentVersion(targetVersion);
        meta.setSha256(target.getSha256());
        meta.setFileSize(target.getFileSize());
        fileMetadataMapper.update(meta);
    }

    public InputStream downloadByKey(String key) {
        return storageService.getInputStream(key);
    }

    public List<VideoResolutionEntity> listVideoResolutions(long fileId) {
        FileMetadata meta = getMetadata(fileId);
        return videoResolutionRepository.findByFileIdAndVersionOrderByResolution(fileId, meta.getCurrentVersion());
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
        if (session == null) throw new NotFoundException("Upload session not found: " + uploadId);
        return new StorageService.PartETag(partNumber,
                storageService.uploadPart(session.key, uploadId, partNumber, data, size));
    }

    @Transactional
    public FileMetadata completeMultipartUpload(String uploadId, List<StorageService.PartETag> parts) {
        MultipartSession session = multipartSessions.remove(uploadId);
        if (session == null) throw new NotFoundException("Upload session not found: " + uploadId);
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
        if (session == null) return;
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
            throw new RuntimeException(e);
        }
    }
}
