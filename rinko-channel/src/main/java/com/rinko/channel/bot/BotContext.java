package com.rinko.channel.bot;

import java.util.Map;

public record BotContext(
    String platformType,
    String botId,
    Map<String, Object> config
) {}
