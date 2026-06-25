package com.rinko.channel.qq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
public class RinkoChannelQqApplication {
    public static void main(String[] args) {
        SpringApplication.run(RinkoChannelQqApplication.class, args);
    }
}
