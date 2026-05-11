package com.rinko.auth.repository

import com.rinko.auth.entity.OAuth2Client
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface OAuth2ClientRepository : ReactiveCrudRepository<OAuth2Client, Long> {

    fun findByClientId(clientId: String): Mono<OAuth2Client>
}
