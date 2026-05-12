package com.rinko.auth

import com.alibaba.druid.spring.boot4.autoconfigure.DruidDataSourceAutoConfigure
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication(exclude = [DruidDataSourceAutoConfigure::class])
@EnableDiscoveryClient
class RinkoAuthApplication

fun main(args: Array<String>) {
    runApplication<RinkoAuthApplication>(*args)
}
