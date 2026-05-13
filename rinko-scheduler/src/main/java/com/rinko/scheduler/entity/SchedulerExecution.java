package com.rinko.scheduler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scheduler_executions")
public class SchedulerExecution {
    @TableId(type = IdType.INPUT)
    private Long id;
    private long jobId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int retryCount;
    private String result;
    private LocalDateTime createdAt;
}
