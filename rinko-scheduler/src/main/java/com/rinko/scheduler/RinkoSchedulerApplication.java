package com.rinko.scheduler;

import com.rinko.infra.config.EnableDruid;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDruid
public class RinkoSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RinkoSchedulerApplication.class, args);
    }
}
