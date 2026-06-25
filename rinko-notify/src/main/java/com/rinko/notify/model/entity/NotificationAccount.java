package com.rinko.notify.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification_accounts")
public class NotificationAccount {
    @TableId(type = IdType.INPUT)
    private Long id;
    /**
     * 提供商类型：SMTP / SENDGRID / ALIYUN_SMS / TENCENT_SMS
     */
    private String provider;
    /**
     * 账户显示名称
     */
    private String name;
    /**
     * JSON 配置内容
     */
    private String config;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
