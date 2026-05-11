package com.rinko.oss.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("video_resolutions")
public class VideoResolutionEntity {
    @Id
    private Long id;
    private long fileId;
    private int version;
    private String resolution;
    private String status;
    private Long fileSize;
    private String storagePath;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
