package com.rinko.log;

import com.rinko.infra.config.EnableDruid;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author rinko
 */
@SpringBootApplication(scanBasePackages = "com.rinko")
@MapperScan("com.rinko.log.**.repository")
@EnableDiscoveryClient
@EnableDruid
@EnableScheduling
public class RinkoLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(RinkoLogApplication.class, args);
    }
}
