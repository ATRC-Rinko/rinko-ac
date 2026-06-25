package com.rinko.scheduler.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scheduler_jobs")
public class SchedulerJob {
    @TableId(type = IdType.INPUT)
    /** 主键ID */
    private Long id;
    /**
     * 任务名称（唯一）
     */
    private String name;
    /**
     * 任务类型
     */
    private String type;
    /**
     * CRON表达式
     */
    private String cronExpression;
    /**
     * 任务配置（JSON格式）
     */
    private String config;
    /**
     * 是否启用
     */
    private boolean enabled;
    /**
     * 最大重试次数
     */
    private int maxRetries;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
