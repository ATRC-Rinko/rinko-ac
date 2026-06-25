package com.rinko.oss.model.entity;

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
    /** 主键ID */
    private Long id;
    /**
     * 原始文件名
     */
    private String originalName;
    /**
     * 文件大小（字节）
     */
    private long fileSize;
    /**
     * 文件内容类型（MIME）
     */
    private String contentType;
    /**
     * 文件SHA256哈希值
     */
    private String sha256;
    /**
     * 存储路径
     */
    private String storagePath;
    /**
     * 存储类型（LOCAL / S3 / OSS）
     */
    private String storageType;
    /**
     * 父目录ID
     */
    private Long parentId;
    @TableField("is_directory")
    /** 是否为目录 */
    private boolean isDirectory;
    /**
     * 当前版本号
     */
    private int currentVersion = 1;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
