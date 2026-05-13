package com.rinko.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification_templates")
public class NotificationTemplate {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String code;
    private String name;
    private String subject;
    private String body;
    private String channels;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
