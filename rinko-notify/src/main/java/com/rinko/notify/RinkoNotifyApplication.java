package com.rinko.notify;

import com.rinko.infra.config.EnableDruid;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.rinko")
@EnableDiscoveryClient
@EnableDruid
public class RinkoNotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(RinkoNotifyApplication.class, args);
    }
}
