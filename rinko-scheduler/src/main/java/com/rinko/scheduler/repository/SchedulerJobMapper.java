package com.rinko.scheduler.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.scheduler.model.entity.SchedulerJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SchedulerJobMapper extends BaseMapper<SchedulerJob> {
}
