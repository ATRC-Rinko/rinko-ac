package com.rinko.notify.model.entity;

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
    /** 主键ID */
    private Long id;
    /** 通知渠道（IN_APP / EMAIL / SMS / DINGTALK / WECOM） */
    private String channel;
    /** 模板编码 */
    private String templateCode;
    /** 收件人 */
    private String recipient;
    /** 通知主题 */
    private String subject;
    /** 通知内容 */
    private String content;
    /** 发送状态（PENDING / SUCCESS / FAILED） */
    private String status;
    @TableField("is_read")
    /** 是否已读 */
    private boolean isRead;
    /** 读取时间 */
    private LocalDateTime readAt;
    /** 错误信息 */
    private String errorMessage;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
