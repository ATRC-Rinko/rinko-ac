package com.rinko.auth.controller

import com.rinko.auth.dto.*
import com.rinko.auth.service.AuthService
import com.rinko.infra.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "用户认证接口")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    fun register(@RequestBody request: RegisterRequest): Mono<ApiResponse<AuthResponse>> {
        return authService.register(request)
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    fun login(@RequestBody request: LoginRequest): Mono<ApiResponse<AuthResponse>> {
        return authService.login(request)
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    fun logout(@RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String): Mono<ApiResponse<MessageResponse>> {
        val token = authHeader.removePrefix("Bearer ")
        return authService.logout(token)
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "刷新 Access Token")
    fun refreshToken(@RequestBody body: Map<String, String>, exchange: ServerWebExchange): Mono<ApiResponse<TokenPair>> {
        val refreshToken = body["refreshToken"]
        if (refreshToken == null) {
            exchange.response.statusCode = HttpStatus.valueOf(400)
            return Mono.just(ApiResponse.error(400, "refreshToken is required"))
        }
        return authService.refreshToken(refreshToken)
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/token/revoke")
    @Operation(summary = "吊销 Refresh Token")
    fun revokeToken(@RequestBody body: Map<String, String>, exchange: ServerWebExchange): Mono<ApiResponse<MessageResponse>> {
        val refreshToken = body["refreshToken"]
        if (refreshToken == null) {
            exchange.response.statusCode = HttpStatus.valueOf(400)
            return Mono.just(ApiResponse.error(400, "refreshToken is required"))
        }
        return authService.revokeToken(refreshToken)
            .map { ApiResponse.success(it) }
    }
}
