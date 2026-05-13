package com.rinko.oss.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_versions")
public class FileVersion {
    @TableId(type = IdType.INPUT)
    private Long id;
    private long fileId;
    private int version;
    private long fileSize;
    private String sha256;
    private String storagePath;
    private LocalDateTime createdAt;
}
