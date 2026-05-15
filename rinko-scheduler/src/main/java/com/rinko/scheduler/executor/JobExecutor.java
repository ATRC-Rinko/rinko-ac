package com.rinko.scheduler.executor;

import com.rinko.scheduler.model.entity.SchedulerJob;

public interface JobExecutor {
    String execute(SchedulerJob job);
    boolean supports(String type);
}
