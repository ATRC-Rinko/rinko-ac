package com.rinko.scheduler.repository;

import com.rinko.scheduler.entity.SchedulerDependency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SchedulerDependencyMapper {

    int insert(SchedulerDependency dep);
    int deleteById(@Param("id") long id);
    List<SchedulerDependency> findByJobId(@Param("jobId") long jobId);
    List<SchedulerDependency> findByDependsOnJobId(@Param("dependsOnJobId") long jobId);
}
