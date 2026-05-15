package com.rinko.notify.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.notify.model.entity.NotificationHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationHistoryMapper extends BaseMapper<NotificationHistory> {

    int batchInsert(@Param("list") List<NotificationHistory> list);
}
