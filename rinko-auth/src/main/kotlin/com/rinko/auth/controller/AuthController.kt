package com.rinko.auth.controller

import com.rinko.auth.dto.*
import com.rinko.auth.service.AuthService
import com.rinko.auth.service.VerificationCodeService
import com.rinko.infra.dto.ApiResponse
import com.rinko.infra.exception.ValidationException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "用户认证接口")
class AuthController(
    private val authService: AuthService,
    private val verificationCodeService: VerificationCodeService
) {

    @PostMapping("/send-code")
    @Operation(summary = "发送邮箱验证码")
    fun sendCode(@RequestBody request: SendCodeRequest): Mono<ApiResponse<SendCodeResponse>> {
        return verificationCodeService.sendCode(request.email)
            .map { ApiResponse.success(it) }
    }

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
    fun refreshToken(@RequestBody body: Map<String, String>): Mono<ApiResponse<TokenPair>> {
        val refreshToken = body["refreshToken"]
            ?: throw ValidationException("refreshToken is required")
        return authService.refreshToken(refreshToken)
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/token/revoke")
    @Operation(summary = "吊销 Refresh Token")
    fun revokeToken(@RequestBody body: Map<String, String>): Mono<ApiResponse<MessageResponse>> {
        val refreshToken = body["refreshToken"]
            ?: throw ValidationException("refreshToken is required")
        return authService.revokeToken(refreshToken)
            .map { ApiResponse.success(it) }
    }
}
