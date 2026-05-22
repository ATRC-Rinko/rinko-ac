package com.rinko.gateway.filter

import com.rinko.gateway.config.GatewayAuthProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.apache.skywalking.apm.toolkit.trace.TraceContext
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
class JwtAuthFilter(
    private val authProperties: GatewayAuthProperties
) : GlobalFilter, Ordered {

    companion object {
        private val log = LoggerFactory.getLogger(JwtAuthFilter::class.java)
        private val pathMatcher = AntPathMatcher()
        const val HEADER_USER_ID = "X-User-Id"
        const val HEADER_USER_ROLES = "X-User-Roles"
    }

    override fun getOrder(): Int = -100

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path

        if (isWhitelisted(path)) {
            return chain.filter(exchange)
        }

        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header")
        }

        val token = authHeader.removePrefix("Bearer ").trim()
        return try {
            val key = Keys.hmacShaKeyFor(authProperties.jwtSecret.toByteArray().let {
                if (it.size < 32) it.copyOf(32) else it
            })
            val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

            val userId = (claims["userId"] as Number).toLong()
            @Suppress("UNCHECKED_CAST")
            val roles = claims["roles"] as? List<String> ?: emptyList()

            val request: ServerHttpRequest = exchange.request.mutate()
                .header(HEADER_USER_ID, userId.toString())
                .header(HEADER_USER_ROLES, roles.joinToString(","))
                .build()
            chain.filter(exchange.mutate().request(request).build())
        } catch (e: Exception) {
            log.debug("JWT validation failed: {}", e.message)
            unauthorized(exchange, "Invalid or expired token")
        }
    }

    private fun isWhitelisted(path: String): Boolean {
        return authProperties.whitelistPaths.any { pattern ->
            pathMatcher.match(pattern, path)
        }
    }

    private fun unauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON
        val body = """
            {"type":"about:blank","title":"Unauthorized","status":401,"detail":"$message"}
        """.trimIndent()
        val buffer: DataBuffer = exchange.response.bufferFactory()
            .wrap(body.toByteArray(StandardCharsets.UTF_8))
        return exchange.response.writeWith(Mono.just(buffer))
    }
}
