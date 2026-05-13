package com.rinko.scheduler.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.scheduler.entity.SchedulerDependency;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SchedulerDependencyMapper extends BaseMapper<SchedulerDependency> {
}
