package com.rinko.scheduler.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scheduler_executions")
public class SchedulerExecution {
    @TableId(type = IdType.INPUT)
    /** 主键ID */
    private Long id;
    /** 任务ID（关联 scheduler_jobs） */
    private long jobId;
    /** 执行状态（RUNNING / SUCCESS / FAILED） */
    private String status;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 重试次数 */
    private int retryCount;
    /** 执行结果 */
    private String result;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
