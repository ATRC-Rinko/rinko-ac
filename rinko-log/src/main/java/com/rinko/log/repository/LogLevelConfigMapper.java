package com.rinko.log.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.log.model.entity.LogLevelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日志级别配置 Mapper（MyBatis-Plus + PostgreSQL）。
 */
@Mapper
public interface LogLevelConfigMapper extends BaseMapper<LogLevelConfig> {
}
