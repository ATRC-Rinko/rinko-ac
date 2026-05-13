package com.rinko.scheduler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scheduler_dependencies")
public class SchedulerDependency {
    @TableId(type = IdType.INPUT)
    private Long id;
    private long jobId;
    private long dependsOnJobId;
    private LocalDateTime createdAt;
}
