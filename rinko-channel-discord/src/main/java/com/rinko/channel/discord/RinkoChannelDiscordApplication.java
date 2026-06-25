package com.rinko.channel.discord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
public class RinkoChannelDiscordApplication {
    public static void main(String[] args) {
        SpringApplication.run(RinkoChannelDiscordApplication.class, args);
    }
}
