package com.rinko.notify.model.vo;

import com.rinko.notify.model.entity.NotificationHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "通知历史VO")
public record NotificationHistoryVO(
        @Schema(description = "主键ID") Long id,
        @Schema(description = "通知渠道（IN_APP / EMAIL / SMS / DINGTALK / WECOM）") String channel,
        @Schema(description = "模板编码") String templateCode,
        @Schema(description = "收件人") String recipient,
        @Schema(description = "通知主题") String subject,
        @Schema(description = "通知内容") String content,
        @Schema(description = "发送状态（PENDING / SUCCESS / FAILED）") String status,
        @Schema(description = "是否已读") boolean isRead,
        @Schema(description = "读取时间") LocalDateTime readAt,
        @Schema(description = "错误信息") String errorMessage,
        @Schema(description = "创建时间") LocalDateTime createdAt) {

    public static NotificationHistoryVO from(NotificationHistory entity) {
        return new NotificationHistoryVO(
                entity.getId(),
                entity.getChannel(),
                entity.getTemplateCode(),
                entity.getRecipient(),
                entity.getSubject(),
                entity.getContent(),
                entity.getStatus(),
                entity.isRead(),
                entity.getReadAt(),
                entity.getErrorMessage(),
                entity.getCreatedAt()
        );
    }
}
