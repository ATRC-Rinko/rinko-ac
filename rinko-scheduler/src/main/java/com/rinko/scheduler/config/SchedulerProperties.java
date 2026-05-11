package com.rinko.scheduler.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rinko.scheduler")
public class SchedulerProperties {
    private int threadPoolSize = 10;
    private int maxRetries = 3;
    private boolean autoStartup = true;
}
