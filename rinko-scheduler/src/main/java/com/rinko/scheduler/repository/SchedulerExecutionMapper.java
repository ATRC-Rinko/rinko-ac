package com.rinko.scheduler.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.scheduler.entity.SchedulerExecution;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SchedulerExecutionMapper extends BaseMapper<SchedulerExecution> {
}
