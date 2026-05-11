package com.rinko.oss.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("file_versions")
public class FileVersion {
    @Id
    private Long id;
    private long fileId;
    private int version;
    private long fileSize;
    private String sha256;
    private String storagePath;
    private LocalDateTime createdAt;
}
