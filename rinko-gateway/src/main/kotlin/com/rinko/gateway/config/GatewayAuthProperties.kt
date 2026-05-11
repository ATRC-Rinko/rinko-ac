package com.rinko.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "rinko.gateway.auth")
class GatewayAuthProperties {
    lateinit var jwtSecret: String
    var whitelistPaths: List<String> = listOf(
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/token/refresh",
        "/oauth2/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/actuator/health"
    )
}
