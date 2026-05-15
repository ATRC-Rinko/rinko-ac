package com.rinko.auth.service

import com.rinko.auth.dto.*
import com.rinko.auth.entity.User
import com.rinko.auth.entity.UserStatus
import com.rinko.auth.repository.UserRepository
import com.rinko.auth.security.JwtTokenProvider
import com.rinko.auth.security.TokenBlacklistService
import com.rinko.infra.exception.UnauthorizedException
import com.rinko.infra.exception.ValidationException
import com.rinko.infra.id.SnowflakeIdGenerator
import org.apache.seata.spring.annotation.GlobalTransactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class AuthService(
    @Value("\${rinko.auth.jwt.access-token-expiration}") private val accessTokenExpiration: Long,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenBlacklistService: TokenBlacklistService,
    private val snowflakeIdGenerator: SnowflakeIdGenerator,
    private val verificationCodeService: VerificationCodeService
) {
    companion object {
        private val log = LoggerFactory.getLogger(AuthService::class.java)
    }

    @GlobalTransactional
    fun register(request: RegisterRequest): Mono<AuthResponse> {
        return userRepository.findByUsername(request.username)
            .hasElement()
            .flatMap { usernameExists ->
                if (usernameExists) {
                    return@flatMap Mono.error<AuthResponse>(ValidationException("Username already exists"))
                }
                userRepository.findByEmail(request.email)
                    .hasElement()
                    .flatMap { emailExists ->
                        if (emailExists) {
                            return@flatMap Mono.error<AuthResponse>(ValidationException("Email already exists"))
                        }
                        verificationCodeService.verifyCode(request.email, request.code)
                            .flatMap { valid ->
                                if (!valid) {
                                    return@flatMap Mono.error<AuthResponse>(ValidationException("Invalid verification code"))
                                }
                                val encodedPw = passwordEncoder.encode(request.password)!!
                                val user = User(
                                    id = snowflakeIdGenerator.nextId(),
                                    username = request.username,
                                    email = request.email,
                                    passwordHash = encodedPw,
                                    status = UserStatus.ACTIVE
                                ).apply { isNewRecord = true }
                                userRepository.save(user)
                                    .flatMap { saved ->
                                        verificationCodeService.deleteCode(request.email)
                                            .then(createTokenResponse(saved))
                                    }
                            }
                    }
            }
    }

    fun login(request: LoginRequest): Mono<AuthResponse> {
        return userRepository.findByUsername(request.username)
            .switchIfEmpty(Mono.error(UnauthorizedException("Invalid username or password")))
            .flatMap { user ->
                if (user.status != UserStatus.ACTIVE) {
                    return@flatMap Mono.error<AuthResponse>(
                        UnauthorizedException("Account is ${user.status.name.lowercase()}")
                    )
                }
                if (!passwordEncoder.matches(request.password, user.passwordHash)) {
                    return@flatMap Mono.error<AuthResponse>(UnauthorizedException("Invalid username or password"))
                }
                val updated = user.copy(updatedAt = LocalDateTime.now()).apply { isNewRecord = false }
                userRepository.save(updated).flatMap { createTokenResponse(it) }
            }
    }

    fun logout(token: String): Mono<MessageResponse> {
        return jwtTokenProvider.validateToken(token)
            .flatMap { valid ->
                if (!valid) {
                    return@flatMap Mono.just(MessageResponse("Token already invalid"))
                }
                val tokenId = jwtTokenProvider.getTokenId(token)
                val expiration = jwtTokenProvider.getTokenExpiration(token)
                tokenBlacklistService.add(tokenId, expiration)
                    .map { MessageResponse("Logged out successfully") }
            }
    }

    fun refreshToken(refreshToken: String): Mono<TokenPair> {
        return jwtTokenProvider.validateToken(refreshToken)
            .flatMap { valid ->
                if (!valid) {
                    return@flatMap Mono.error<TokenPair>(UnauthorizedException("Invalid refresh token"))
                }
                val userId = jwtTokenProvider.getUserId(refreshToken)
                userRepository.findById(userId)
                    .switchIfEmpty(Mono.error(UnauthorizedException("User not found")))
                    .flatMap { user ->
                        val roles = emptyList<String>()
                        val access = jwtTokenProvider.generateAccessToken(user.id, user.username, roles)
                        val refresh = jwtTokenProvider.generateRefreshToken(user.id, user.username)
                        Mono.just(TokenPair(access, refresh, 900))
                    }
            }
    }

    fun revokeToken(refreshToken: String): Mono<MessageResponse> {
        return jwtTokenProvider.validateToken(refreshToken)
            .flatMap { valid ->
                if (!valid) {
                    return@flatMap Mono.just(MessageResponse("Token already invalidated"))
                }
                val tokenId = jwtTokenProvider.getTokenId(refreshToken)
                val expiration = jwtTokenProvider.getTokenExpiration(refreshToken)
                tokenBlacklistService.add(tokenId, expiration)
                    .map { MessageResponse("Token revoked successfully") }
            }
    }

    private fun createTokenResponse(user: User): Mono<AuthResponse> {
        val roles = emptyList<String>()
        val accessToken = jwtTokenProvider.generateAccessToken(user.id, user.username, roles)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id, user.username)
        return Mono.just(
            AuthResponse(
                tokenPair = TokenPair(accessToken, refreshToken, accessTokenExpiration),
                userId = user.id,
                username = user.username
            )
        )
    }
}
