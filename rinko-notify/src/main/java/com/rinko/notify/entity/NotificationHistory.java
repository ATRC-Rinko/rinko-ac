package com.rinko.notify.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("notification_history")
public class NotificationHistory {
    @Id
    private Long id;
    private String channel;
    private String templateCode;
    private String recipient;
    private String subject;
    private String content;
    private String status;
    private boolean isRead;
    private LocalDateTime readAt;
    private String errorMessage;
    private LocalDateTime createdAt;
}
