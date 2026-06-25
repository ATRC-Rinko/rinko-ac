package com.rinko.channel.dingtalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
public class RinkoChannelDingtalkApplication {
    public static void main(String[] args) {
        SpringApplication.run(RinkoChannelDingtalkApplication.class, args);
    }
}
