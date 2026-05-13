package com.rinko.scheduler.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rinko.infra.exception.InternalException;
import com.rinko.infra.id.SnowflakeIdGenerator;
import com.rinko.scheduler.entity.SchedulerDependency;
import com.rinko.scheduler.entity.SchedulerExecution;
import com.rinko.scheduler.entity.SchedulerJob;
import com.rinko.scheduler.repository.SchedulerDependencyMapper;
import com.rinko.scheduler.repository.SchedulerExecutionMapper;
import com.rinko.scheduler.repository.SchedulerJobMapper;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final SchedulerJobMapper jobMapper;
    private final SchedulerExecutionMapper executionMapper;
    private final SchedulerDependencyMapper dependencyMapper;
    private final Scheduler quartzScheduler;
    private final RabbitTemplate rabbitTemplate;
    private final SnowflakeIdGenerator idGenerator;

    public List<SchedulerJob> listJobs() {
        return jobMapper.selectList(Wrappers.<SchedulerJob>lambdaQuery().orderByAsc(SchedulerJob::getName));
    }

    @Transactional
    public SchedulerJob createJob(SchedulerJob job) {
        job.setId(idGenerator.nextId());
        jobMapper.insert(job);
        if (job.isEnabled() && job.getCronExpression() != null) {
            scheduleQuartzJob(job);
        }
        return job;
    }

    @Transactional
    public void deleteJob(long id) {
        try {
            quartzScheduler.deleteJob(JobKey.jobKey("job-" + id));
        } catch (SchedulerException ignored) {}
        jobMapper.deleteById(id);
    }

    public void triggerJob(long id) {
        try {
            quartzScheduler.triggerJob(JobKey.jobKey("job-" + id));
        } catch (SchedulerException e) {
            throw new InternalException("Failed totrigger job", e);
        }
    }

    public void pauseJob(long id) {
        try {
            quartzScheduler.pauseJob(JobKey.jobKey("job-" + id));
        } catch (SchedulerException e) {
            throw new InternalException("Failed topause job", e);
        }
    }

    public void resumeJob(long id) {
        try {
            quartzScheduler.resumeJob(JobKey.jobKey("job-" + id));
        } catch (SchedulerException e) {
            throw new InternalException("Failed toresume job", e);
        }
    }

    private void scheduleQuartzJob(SchedulerJob job) {
        try {
            JobDetail detail = JobBuilder.newJob(QuartzJobBean.class)
                    .withIdentity("job-" + job.getId())
                    .usingJobData("jobId", String.valueOf(job.getId()))
                    .storeDurably()
                    .build();
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger-" + job.getId())
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                    .build();
            quartzScheduler.scheduleJob(detail, trigger);
        } catch (SchedulerException e) {
            throw new InternalException("Failed toschedule Quartz job", e);
        }
    }

    public SchedulerExecution recordStart(long jobId) {
        SchedulerExecution exec = new SchedulerExecution();
        exec.setId(idGenerator.nextId());
        exec.setJobId(jobId);
        exec.setStatus("RUNNING");
        executionMapper.insert(exec);
        return exec;
    }

    public void recordEnd(SchedulerExecution exec, String status, String result) {
        exec.setStatus(status);
        exec.setResult(result);
        executionMapper.updateById(exec);
    }

    public void alertFailure(SchedulerJob job, String error) {
        rabbitTemplate.convertAndSend("notify.queue",
                Map.of("type", "JOB_FAILED", "jobName", job.getName(), "error", error, "timestamp", new Date().toString()));
    }

    public List<SchedulerExecution> getExecutions(long jobId) {
        return executionMapper.selectList(Wrappers.lambdaQuery(SchedulerExecution.class)
                .eq(SchedulerExecution::getJobId, jobId)
                .orderByDesc(SchedulerExecution::getStartTime));
    }

    @Transactional
    public SchedulerDependency addDependency(long jobId, long dependsOnJobId) {
        SchedulerDependency dep = new SchedulerDependency();
        dep.setId(idGenerator.nextId());
        dep.setJobId(jobId);
        dep.setDependsOnJobId(dependsOnJobId);
        dependencyMapper.insert(dep);
        return dep;
    }

    public void removeDependency(long depId) {
        dependencyMapper.deleteById(depId);
    }

    public List<SchedulerDependency> getDownstream(long jobId) {
        return dependencyMapper.selectList(Wrappers.lambdaQuery(SchedulerDependency.class)
                .eq(SchedulerDependency::getDependsOnJobId, jobId));
    }

    public List<SchedulerDependency> getUpstream(long jobId) {
        return dependencyMapper.selectList(Wrappers.lambdaQuery(SchedulerDependency.class)
                .eq(SchedulerDependency::getJobId, jobId));
    }
}
