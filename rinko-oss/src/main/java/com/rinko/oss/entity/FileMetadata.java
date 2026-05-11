package com.rinko.oss.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("file_metadata")
public class FileMetadata {
    @Id
    private Long id;
    private String originalName;
    private long fileSize;
    private String contentType;
    private String sha256;
    private String storagePath;
    private String storageType;
    private Long parentId;
    private boolean isDirectory;
    private int currentVersion = 1;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
