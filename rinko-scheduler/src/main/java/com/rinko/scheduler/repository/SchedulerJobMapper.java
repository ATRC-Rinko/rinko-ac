package com.rinko.scheduler.repository;

import com.rinko.scheduler.entity.SchedulerJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SchedulerJobMapper {

    int insert(SchedulerJob job);
    int update(SchedulerJob job);
    int deleteById(@Param("id") long id);
    List<SchedulerJob> findAll();
    SchedulerJob findById(@Param("id") long id);
    SchedulerJob findByName(@Param("name") String name);
}
