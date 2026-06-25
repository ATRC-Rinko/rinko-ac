package com.rinko.channel.wechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
public class RinkoChannelWechatApplication {
    public static void main(String[] args) {
        SpringApplication.run(RinkoChannelWechatApplication.class, args);
    }
}
