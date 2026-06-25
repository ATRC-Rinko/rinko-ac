package com.rinko.gateway.filter

import com.rinko.gateway.config.GatewayAuthProperties
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.core.publisher.Mono

class JwtAuthFilterTest : StringSpec({

    "whitelisted paths should pass without JWT" {
        val props = GatewayAuthProperties()
        props.jwtSecret = "test-secret-key-for-jwt-testing-minimum-32-bytes!!"
        val filter = JwtAuthFilter(props)

        val request = MockServerHttpRequest.get("/actuator/health").build()
        val exchange = MockServerWebExchange.from(request)
        val chain = Mockito.mock(GatewayFilterChain::class.java)
        Mockito.`when`(chain.filter(exchange)).thenReturn(Mono.empty())

        val result = filter.filter(exchange, chain)
        result shouldBe Mono.empty<Any>()
    }

    "request without Authorization header should return 401" {
        val props = GatewayAuthProperties()
        props.jwtSecret = "test-secret-key-for-jwt-testing-minimum-32-bytes!!"
        val filter = JwtAuthFilter(props)

        val request = MockServerHttpRequest.get("/api/v1/auth/users").build()
        val exchange = MockServerWebExchange.from(request)
        val chain = Mockito.mock(GatewayFilterChain::class.java)

        filter.filter(exchange, chain).block()
        exchange.response.statusCode?.value() shouldBe 401
    }
})
