package com.rinko.scheduler.service;

import com.rinko.scheduler.executor.JobExecutor;
import com.rinko.scheduler.model.entity.SchedulerDependency;
import com.rinko.scheduler.model.entity.SchedulerExecution;
import com.rinko.scheduler.model.entity.SchedulerJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuartzJobBean implements Job {

    private static final Logger log = LoggerFactory.getLogger(QuartzJobBean.class);
    private static SchedulerService schedulerService;
    private static List<JobExecutor> executors;

    public QuartzJobBean(SchedulerService schedulerService, List<JobExecutor> executors) {
        QuartzJobBean.schedulerService = schedulerService;
        QuartzJobBean.executors = executors;
    }

    @Override
    public void execute(JobExecutionContext context) {
        long jobId = Long.parseLong(context.getJobDetail().getJobDataMap().getString("jobId"));
        SchedulerJob job = schedulerService.getJobById(jobId);
        if (job == null) return;

        SchedulerExecution exec = schedulerService.recordStart(jobId);
        JobExecutor executor = executors.stream().filter(e -> e.supports(job.getType())).findFirst().orElse(null);
        if (executor == null) {
            schedulerService.recordEnd(exec, "FAILED", "No executor for type: " + job.getType());
            return;
        }

        int maxRetries = job.getMaxRetries() > 0 ? job.getMaxRetries() : 3;
        for (int i = 0; i <= maxRetries; i++) {
            try {
                String result = executor.execute(job);
                schedulerService.recordEnd(exec, "SUCCESS", result);
                log.info("Job {} ({}) completed successfully", job.getName(), jobId);
                triggerDownstream(jobId);
                return;
            } catch (Exception e) {
                log.warn("Job {} failed (attempt {}/{}): {}", job.getName(), i + 1, maxRetries + 1, e.getMessage());
                if (i < maxRetries) {
                    try {
                        Thread.sleep((long) Math.pow(i + 1, 2) * 1000L);
                    } catch (InterruptedException ignored) {
                    }
                    exec.setRetryCount(i + 1);
                } else {
                    schedulerService.recordEnd(exec, "FAILED", e.getMessage());
                    schedulerService.alertFailure(job, e.getMessage());
                }
            }
        }
    }

    private void triggerDownstream(long jobId) {
        List<SchedulerDependency> downstream = schedulerService.getDownstream(jobId);
        for (SchedulerDependency dep : downstream) {
            schedulerService.triggerJob(dep.getJobId());
        }
    }
}
