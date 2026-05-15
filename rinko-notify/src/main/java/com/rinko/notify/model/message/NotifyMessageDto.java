package com.rinko.notify.model.message;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 *
 * @param channel
 * @param templateCode
 * @param recipient
 * @param variables
 */
@Schema(description = "MQ消息请求")
public record NotifyMessageDto(String channel
        , String templateCode
        , String recipient
        , Map<String, String> variables
) {
}
