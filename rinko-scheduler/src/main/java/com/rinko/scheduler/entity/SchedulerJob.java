package com.rinko.scheduler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scheduler_jobs")
public class SchedulerJob {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private String type;
    private String cronExpression;
    private String config;
    private boolean enabled;
    private int maxRetries;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
