package com.rinko.gateway

import com.alibaba.druid.spring.boot4.autoconfigure.DruidDataSourceAutoConfigure
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication(exclude = [DruidDataSourceAutoConfigure::class], scanBasePackages = ["com.rinko"])
@EnableDiscoveryClient
class RinkoGatewayApplication

fun main(args: Array<String>) {
    runApplication<RinkoGatewayApplication>(*args)
}
