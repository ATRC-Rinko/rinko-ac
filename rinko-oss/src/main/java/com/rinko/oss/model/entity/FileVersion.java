package com.rinko.oss.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_versions")
public class FileVersion {
    @TableId(type = IdType.INPUT)
    /** 主键ID */
    private Long id;
    /**
     * 文件ID（关联 file_metadata）
     */
    private long fileId;
    /**
     * 版本号
     */
    private int version;
    /**
     * 文件大小（字节）
     */
    private long fileSize;
    /**
     * 文件SHA256哈希值
     */
    private String sha256;
    /**
     * 存储路径
     */
    private String storagePath;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
