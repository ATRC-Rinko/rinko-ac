package com.rinko.oss.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_metadata")
public class FileMetadata {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String originalName;
    private long fileSize;
    private String contentType;
    private String sha256;
    private String storagePath;
    private String storageType;
    private Long parentId;
    @TableField("is_directory")
    private boolean isDirectory;
    private int currentVersion = 1;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
