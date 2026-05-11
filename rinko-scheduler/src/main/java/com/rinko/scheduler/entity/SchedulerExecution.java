package com.rinko.scheduler.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("scheduler_executions")
public class SchedulerExecution {
    @Id
    private Long id;
    private long jobId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int retryCount;
    private String result;
    private LocalDateTime createdAt;
}
