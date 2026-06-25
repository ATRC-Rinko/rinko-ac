package com.rinko.auth.service

import com.rinko.auth.repository.OAuth2ClientRepository
import com.rinko.auth.security.JwtTokenProvider
import com.rinko.infra.exception.UnauthorizedException
import com.rinko.infra.exception.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class OAuth2Service(
    private val clientRepository: OAuth2ClientRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    companion object {
        private val log = LoggerFactory.getLogger(OAuth2Service::class.java)
        private val secureRandom = SecureRandom()
    }

    private val authorizationCodes = ConcurrentHashMap<String, AuthorizationCodeEntry>()

    data class AuthorizationCodeEntry(
        val code: String,
        val clientId: String,
        val redirectUri: String,
        val scopes: String,
        val userId: Long?,
        val expiresAt: Long
    )

    data class AuthorizeResult(
        val redirectUrl: String,
        val code: String,
        val state: String?
    )

    // ===== T021: Authorization Code — Authorize =====
    fun authorize(
        clientId: String,
        responseType: String,
        redirectUri: String,
        scope: String?,
        state: String?
    ): Mono<AuthorizeResult> {
        if (responseType != "code") {
            return Mono.error(ValidationException("Unsupported response_type: $responseType"))
        }
        return clientRepository.findByClientId(clientId)
            .switchIfEmpty(Mono.error(ValidationException("Unknown client_id: $clientId")))
            .flatMap { client ->
                if (!client.enabled) {
                    return@flatMap Mono.error<AuthorizeResult>(UnauthorizedException("Client is disabled"))
                }
                val allowedUris = client.redirectUris.split(",").map { it.trim() }
                if (redirectUri !in allowedUris) {
                    return@flatMap Mono.error<AuthorizeResult>(ValidationException("redirect_uri not allowed"))
                }
                val code = generateAuthorizationCode()
                val expiresAt = System.currentTimeMillis() + 600_000 // 10 minutes
                val entry = AuthorizationCodeEntry(code, clientId, redirectUri, scope ?: "", null, expiresAt)
                authorizationCodes[code] = entry
                val sb = StringBuilder("$redirectUri?code=$code")
                if (!state.isNullOrEmpty()) sb.append("&state=$state")
                Mono.just(AuthorizeResult(sb.toString(), code, state))
            }
    }

    // ===== T021: Authorization Code — Token Exchange =====
    fun tokenAuthorizationCode(
        clientId: String,
        clientSecret: String,
        code: String,
        redirectUri: String
    ): Mono<Map<String, Any>> {
        return clientRepository.findByClientId(clientId)
            .switchIfEmpty(Mono.error(UnauthorizedException("Invalid client credentials")))
            .flatMap { client ->
                if (!passwordEncoder.matches(clientSecret, client.clientSecret)) {
                    return@flatMap Mono.error<Map<String, Any>>(UnauthorizedException("Invalid client credentials"))
                }
                val entry = authorizationCodes[code]
                    ?: return@flatMap Mono.error<Map<String, Any>>(UnauthorizedException("Invalid or expired authorization code"))
                if (entry.clientId != clientId) {
                    return@flatMap Mono.error<Map<String, Any>>(UnauthorizedException("Code was not issued to this client"))
                }
                if (entry.redirectUri != redirectUri) {
                    return@flatMap Mono.error<Map<String, Any>>(UnauthorizedException("redirect_uri mismatch"))
                }
                if (System.currentTimeMillis() > entry.expiresAt) {
                    authorizationCodes.remove(code)
                    return@flatMap Mono.error<Map<String, Any>>(UnauthorizedException("Authorization code expired"))
                }
                authorizationCodes.remove(code)
                val userId = entry.userId ?: 0L
                val username = "oauth2_user_${entry.clientId}"
                val accessToken = jwtTokenProvider.generateAccessToken(
                    userId,
                    username,
                    entry.scopes.split(",").filter { it.isNotBlank() })
                val refreshToken = jwtTokenProvider.generateRefreshToken(userId, username)
                val expiresIn = 900L
                Mono.just(
                    mapOf(
                        "access_token" to accessToken,
                        "token_type" to "Bearer",
                        "expires_in" to expiresIn,
                        "refresh_token" to refreshToken,
                        "scope" to entry.scopes
                    )
                )
            }
    }

    // ===== T022: Client Credentials =====
    fun tokenClientCredentials(
        clientId: String,
        clientSecret: String,
        scope: String?
    ): Mono<Map<String, Any>> {
        return clientRepository.findByClientId(clientId)
            .switchIfEmpty(Mono.error(UnauthorizedException("Invalid client credentials")))
            .flatMap { client ->
                if (!client.enabled) {
                    return@flatMap Mono.error<Map<String, Any>>(UnauthorizedException("Client is disabled"))
                }
                if (!passwordEncoder.matches(clientSecret, client.clientSecret)) {
                    return@flatMap Mono.error<Map<String, Any>>(UnauthorizedException("Invalid client credentials"))
                }
                val allowedGrants = client.grantTypes.split(",").map { it.trim() }
                if ("client_credentials" !in allowedGrants) {
                    return@flatMap Mono.error<Map<String, Any>>(UnauthorizedException("client_credentials grant not allowed for this client"))
                }
                val scopes = scope ?: client.scopes
                val accessToken = jwtTokenProvider.generateAccessToken(
                    client.id,
                    client.clientId,
                    scopes.split(",").filter { it.isNotBlank() })
                val expiresIn = client.accessTokenTtlSeconds.toLong()
                Mono.just(
                    mapOf(
                        "access_token" to accessToken,
                        "token_type" to "Bearer",
                        "expires_in" to expiresIn,
                        "scope" to scopes
                    )
                )
            }
    }

    /** 定时清理过期的授权码，每 5 分钟执行一次。 */
    @Scheduled(fixedRate = 300_000)
    fun cleanupExpiredCodes() {
        val now = System.currentTimeMillis()
        val expired = authorizationCodes.entries.filter { it.value.expiresAt < now }
        expired.forEach { authorizationCodes.remove(it.key) }
        if (expired.isNotEmpty()) {
            log.debug("Cleaned up {} expired authorization codes", expired.size)
        }
    }

    private fun generateAuthorizationCode(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
