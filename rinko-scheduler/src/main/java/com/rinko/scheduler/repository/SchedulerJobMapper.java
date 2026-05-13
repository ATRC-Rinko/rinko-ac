package com.rinko.scheduler.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.scheduler.entity.SchedulerJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SchedulerJobMapper extends BaseMapper<SchedulerJob> {
}
