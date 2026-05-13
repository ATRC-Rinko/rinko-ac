package com.rinko.oss.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video_resolutions")
public class VideoResolutionEntity {
    @TableId(type = IdType.INPUT)
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
