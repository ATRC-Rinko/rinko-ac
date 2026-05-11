package com.rinko.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class RinkoAuthApplication

fun main(args: Array<String>) {
    runApplication<RinkoAuthApplication>(*args)
}
