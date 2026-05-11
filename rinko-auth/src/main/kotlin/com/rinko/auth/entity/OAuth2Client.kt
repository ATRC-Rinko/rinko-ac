package com.rinko.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("oauth2_clients")
data class OAuth2Client(
    @Id val id: Long,
    val clientId: String,
    val clientSecret: String,
    val redirectUris: String,
    val grantTypes: String,
    val scopes: String = "",
    val accessTokenTtlSeconds: Int = 3600,
    val refreshTokenTtlSeconds: Int = 2592000,
    val enabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
