package com.rinko.infra.web

import com.rinko.infra.dto.ApiResponse
import com.rinko.infra.exception.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class ReactiveGlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ReactiveGlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException, exchange: ServerWebExchange): Mono<ApiResponse<*>> {
        log.error("Validation error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(400)
        return Mono.just(ApiResponse.error<String>(400, ex.errorMessage))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException, exchange: ServerWebExchange): Mono<ApiResponse<*>> {
        log.error("Unauthorized error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(401)
        return Mono.just(ApiResponse.error<String>(401, ex.errorMessage))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(ex: ForbiddenException, exchange: ServerWebExchange): Mono<ApiResponse<*>> {
        log.error("Forbidden error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(403)
        return Mono.just(ApiResponse.error<String>(403, ex.errorMessage))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException, exchange: ServerWebExchange): Mono<ApiResponse<*>> {
        log.error("Not Found error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(404)
        return Mono.just(ApiResponse.error<String>(404, ex.errorMessage))
    }

    @ExceptionHandler(InternalException::class)
    fun handleInternal(ex: InternalException, exchange: ServerWebExchange): Mono<ApiResponse<*>> {
        log.error("Internal error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(500)
        return Mono.just(ApiResponse.error<String>(500, ex.errorMessage))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception, exchange: ServerWebExchange): Mono<ApiResponse<*>> {
        log.error("Unhandled exception", ex)
        ex.printStackTrace()
        exchange.response.statusCode = HttpStatus.valueOf(500)
        return Mono.just(ApiResponse.error<String>(500, "Internal Server Error"))
    }
}
