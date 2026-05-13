package com.rinko.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification_history")
public class NotificationHistory {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String channel;
    private String templateCode;
    private String recipient;
    private String subject;
    private String content;
    private String status;
    @TableField("is_read")
    private boolean isRead;
    private LocalDateTime readAt;
    private String errorMessage;
    private LocalDateTime createdAt;
}
