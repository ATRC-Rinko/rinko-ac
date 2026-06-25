package com.rinko.channel.model.vo;

public record ChannelStatusVO(
    String platform,
    String botId,
    String state,
    String detail,
    long messageCount
) {}
