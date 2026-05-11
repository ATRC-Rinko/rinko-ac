package com.rinko.notify.repository;

import com.rinko.notify.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationTemplateMapper {

    int insert(NotificationTemplate template);

    int update(NotificationTemplate template);

    int deleteById(@Param("id") long id);

    List<NotificationTemplate> findAll();

    NotificationTemplate findByCode(@Param("code") String code);

    NotificationTemplate findById(@Param("id") long id);
}
