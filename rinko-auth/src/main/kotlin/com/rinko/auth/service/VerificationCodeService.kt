package com.rinko.auth.service

import com.rinko.auth.dto.SendCodeResponse
import com.rinko.auth.repository.UserRepository
import com.rinko.infra.exception.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.security.SecureRandom
import java.time.Duration

@Service
class VerificationCodeService(
    @Value("\${rinko.auth.verification.code-length:6}") private val codeLength: Int,
    @Value("\${rinko.auth.verification.ttl-seconds:300}") private val ttlSeconds: Long,
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val rabbitTemplate: RabbitTemplate?,
    private val userRepository: UserRepository
) {
    companion object {
        private val log = LoggerFactory.getLogger(VerificationCodeService::class.java)
        private val RANDOM = SecureRandom()
        private const val KEY_PREFIX = "auth:verify-code:"
    }

    fun sendCode(email: String): Mono<SendCodeResponse> {
        return userRepository.findByEmail(email)
            .hasElement()
            .flatMap { exists ->
                if (exists) {
                    return@flatMap Mono.error<SendCodeResponse>(ValidationException("Email already registered"))
                }
                doSendCode(email)
            }
    }

    private fun doSendCode(email: String): Mono<SendCodeResponse> {
        val key = KEY_PREFIX + email
        return redisTemplate.hasKey(key)
            .flatMap { exists ->
                if (exists) {
                    val remaining = redisTemplate.getExpire(key)
                        .map { it.seconds }
                        .defaultIfEmpty(ttlSeconds)
                    return@flatMap remaining.map { Mono.just(SendCodeResponse("Code already sent", it.toInt())) }
                        .flatMap { it }
                }
                val code = generateCode()
                redisTemplate.opsForValue().set(key, code, Duration.ofSeconds(ttlSeconds))
                    .then(Mono.fromCallable {
                        sendEmail(email, code)
                    })
                    .thenReturn(SendCodeResponse("Verification code sent", ttlSeconds.toInt()))
            }
    }

    fun verifyCode(email: String, code: String): Mono<Boolean> {
        val key = KEY_PREFIX + email
        return redisTemplate.opsForValue().get(key)
            .map { storedCode -> storedCode == code }
            .switchIfEmpty(Mono.just(false))
    }

    fun deleteCode(email: String): Mono<Void> {
        val key = KEY_PREFIX + email
        // key 过期也不影响：验证码已经失效，删除只是清理
        return redisTemplate.delete(key).onErrorResume { Mono.empty() }.then()
    }

    private fun generateCode(): String {
        val min = Math.pow(10.0, (codeLength - 1).toDouble()).toInt()
        val max = Math.pow(10.0, codeLength.toDouble()).toInt() - 1
        val num = RANDOM.nextInt(max - min + 1) + min
        return num.toString()
    }

    private fun sendEmail(email: String, code: String) {
        rabbitTemplate?.let {
            val payload = """
                |{"channel":"EMAIL","templateCode":"verification_code","recipient":"$email","variables":{"code":"$code"}}
            """.trimMargin()
            it.convertAndSend("notify.queue", payload)
            log.info("Verification code sent to {}", email)
        }
    }
}
