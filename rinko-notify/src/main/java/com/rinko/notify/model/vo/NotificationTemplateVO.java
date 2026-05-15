package com.rinko.notify.model.vo;

import com.rinko.notify.model.entity.NotificationTemplate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "通知模板VO")
public record NotificationTemplateVO(
        @Schema(description = "主键ID") Long id,
        @Schema(description = "模板编码（唯一）") String code,
        @Schema(description = "模板名称") String name,
        @Schema(description = "模板主题") String subject,
        @Schema(description = "模板正文") String body,
        @Schema(description = "适用的通知渠道（多个用逗号分隔）") String channels,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {

    public static NotificationTemplateVO from(NotificationTemplate entity) {
        return new NotificationTemplateVO(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getSubject(),
                entity.getBody(),
                entity.getChannels(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
