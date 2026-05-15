package com.rinko.scheduler.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scheduler_dependencies")
public class SchedulerDependency {
    @TableId(type = IdType.INPUT)
    /** 主键ID */
    private Long id;
    /** 当前任务ID */
    private long jobId;
    /** 依赖的前置任务ID */
    private long dependsOnJobId;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
