package com.rinko.notify.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("notification_templates")
public class NotificationTemplate {
    @Id
    private Long id;
    private String code;
    private String name;
    private String subject;
    private String body;
    private String channels;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
