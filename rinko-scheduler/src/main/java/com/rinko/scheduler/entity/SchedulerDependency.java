package com.rinko.scheduler.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("scheduler_dependencies")
public class SchedulerDependency {
    @Id
    private Long id;
    private long jobId;
    private long dependsOnJobId;
    private LocalDateTime createdAt;
}
