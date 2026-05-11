package com.rinko.auth.repository

import com.rinko.auth.entity.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface UserRepository : ReactiveCrudRepository<User, Long> {

    fun findByUsername(username: String): Mono<User>

    fun findByEmail(email: String): Mono<User>
}
