package com.rinko.oss.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video_resolutions")
public class VideoResolutionEntity {
    @TableId(type = IdType.INPUT)
    /** 主键ID */
    private Long id;
    /** 文件ID（关联 file_metadata） */
    private long fileId;
    /** 文件版本号 */
    private int version;
    /** 视频分辨率（如 1920x1080） */
    private String resolution;
    /** 转码状态（PENDING / PROCESSING / COMPLETED / FAILED） */
    private String status;
    /** 转码后文件大小（字节） */
    private Long fileSize;
    /** 转码后存储路径 */
    private String storagePath;
    /** 错误信息 */
    private String errorMessage;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
