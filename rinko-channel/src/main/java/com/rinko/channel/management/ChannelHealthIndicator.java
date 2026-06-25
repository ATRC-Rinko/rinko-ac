package com.rinko.channel.management;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

public class ChannelHealthIndicator implements HealthIndicator {

    private final ChannelManager channelManager;

    public ChannelHealthIndicator(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public Health health() {
        var bots = channelManager.listBots();
        if (bots.isEmpty()) {
            return Health.down().withDetail("reason", "No bots registered").build();
        }
        long connected = bots.stream()
            .filter(b -> "CONNECTED".equals(b.state())).count();
        return Health.up()
            .withDetail("total", bots.size())
            .withDetail("connected", connected)
            .build();
    }
}
