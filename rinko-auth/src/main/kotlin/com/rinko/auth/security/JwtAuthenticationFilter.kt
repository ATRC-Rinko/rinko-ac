package com.rinko.auth.security

import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : WebFilter {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        val token = authHeader?.removePrefix(BEARER_PREFIX)

        return if (token != null) {
            jwtTokenProvider.validateToken(token)
                .flatMap { valid ->
                    if (valid) {
                        val auth = jwtTokenProvider.getAuthentication(token)
                        chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
                    } else {
                        chain.filter(exchange)
                    }
                }
        } else {
            chain.filter(exchange)
        }
    }
}
