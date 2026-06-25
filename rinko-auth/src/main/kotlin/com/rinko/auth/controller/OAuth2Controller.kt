package com.rinko.auth.controller

import com.rinko.auth.service.OAuth2Service
import com.rinko.infra.dto.ApiResponse
import com.rinko.infra.exception.ValidationException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.net.URI

@RestController
@RequestMapping("/oauth2")
@Tag(name = "OAuth 2.0", description = "OAuth 2.0 授权接口")
class OAuth2Controller(
    private val oauth2Service: OAuth2Service
) {

    @GetMapping("/authorize")
    @Operation(summary = "OAuth2 授权端点 (Authorization Code)")
    fun authorize(
        @RequestParam("client_id") clientId: String,
        @RequestParam("response_type") responseType: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam(value = "scope", required = false) scope: String?,
        @RequestParam(value = "state", required = false) state: String?
    ): Mono<ResponseEntity<Void>> {
        return oauth2Service.authorize(clientId, responseType, redirectUri, scope, state)
            .map { result ->
                ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(result.redirectUrl))
                    .build()
            }
    }

    @PostMapping("/token")
    @Operation(summary = "OAuth2 Token 端点")
    fun token(
        @RequestParam("grant_type") grantType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("client_secret") clientSecret: String,
        @RequestParam(value = "code", required = false) code: String?,
        @RequestParam(value = "redirect_uri", required = false) redirectUri: String?,
        @RequestParam(value = "scope", required = false) scope: String?
    ): Mono<ApiResponse<Map<String, Any>>> {
        return when (grantType) {
            "authorization_code" -> {
                if (code == null || redirectUri == null) {
                    throw ValidationException("code and redirect_uri are required")
                }
                oauth2Service.tokenAuthorizationCode(clientId, clientSecret, code, redirectUri)
                    .map { ApiResponse.success(it) }
            }

            "client_credentials" -> {
                oauth2Service.tokenClientCredentials(clientId, clientSecret, scope)
                    .map { ApiResponse.success(it) }
            }

            else -> {
                throw ValidationException("Unsupported grant_type: $grantType")
            }
        }
    }
}
