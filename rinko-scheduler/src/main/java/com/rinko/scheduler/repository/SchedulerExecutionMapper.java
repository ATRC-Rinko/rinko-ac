package com.rinko.scheduler.repository;

import com.rinko.scheduler.entity.SchedulerExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SchedulerExecutionMapper {

    int insert(SchedulerExecution execution);
    int update(SchedulerExecution execution);
    List<SchedulerExecution> findByJobId(@Param("jobId") long jobId);
}
