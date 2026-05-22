package com.rinko.gateway.config

import com.rinko.infra.web.CorsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class GatewayCorsConfig {

    @Bean
    fun corsWebFilter(corsProperties: CorsProperties): CorsWebFilter {
        val config = CorsConfiguration().apply {
            // 使用 allowedOriginPatterns 替代 allowedOrigins，支持 "*"
            corsProperties.allowedOrigins.forEach { pattern ->
                this.addAllowedOriginPattern(pattern)
            }

            allowedMethods = corsProperties.allowedMethods
            allowedHeaders = corsProperties.allowedHeaders
            allowCredentials = corsProperties.isAllowCredentials
            maxAge = corsProperties.maxAge
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }

        return CorsWebFilter(source)
    }
}