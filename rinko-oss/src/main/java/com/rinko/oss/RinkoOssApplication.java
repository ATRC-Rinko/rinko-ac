package com.rinko.oss;

import com.rinko.infra.config.EnableDruid;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
@EnableDruid
@EnableAsync
public class RinkoOssApplication {

    public static void main(String[] args) {
        SpringApplication.run(RinkoOssApplication.class, args);
    }
}
