package com.rinko.notify.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.notify.model.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplate> {
}
