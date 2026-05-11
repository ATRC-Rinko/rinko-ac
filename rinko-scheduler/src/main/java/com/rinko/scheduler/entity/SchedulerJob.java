package com.rinko.scheduler.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("scheduler_jobs")
public class SchedulerJob {
    @Id
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
