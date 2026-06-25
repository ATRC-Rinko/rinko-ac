package com.rinko.notify.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification_templates")
public class NotificationTemplate {
    @TableId(type = IdType.INPUT)
    /** 主键ID */
    private Long id;
    /**
     * 模板编码（唯一）
     */
    private String code;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板主题
     */
    private String subject;
    /**
     * 模板正文
     */
    private String body;
    /**
     * 适用的通知渠道（多个用逗号分隔）
     */
    private String channels;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
