package com.rinko.infra.web

import com.rinko.infra.dto.ProblemDetail
import com.rinko.infra.exception.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class ReactiveGlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ReactiveGlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException, exchange: ServerWebExchange): Mono<ProblemDetail> {
        log.error("Validation error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(ex.httpStatus.value())
        exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON
        return Mono.just(ex.toProblemDetail())
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException, exchange: ServerWebExchange): Mono<ProblemDetail> {
        log.error("Unauthorized error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(ex.httpStatus.value())
        exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON
        return Mono.just(ex.toProblemDetail())
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(ex: ForbiddenException, exchange: ServerWebExchange): Mono<ProblemDetail> {
        log.error("Forbidden error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(ex.httpStatus.value())
        exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON
        return Mono.just(ex.toProblemDetail())
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException, exchange: ServerWebExchange): Mono<ProblemDetail> {
        log.error("Not Found error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(ex.httpStatus.value())
        exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON
        return Mono.just(ex.toProblemDetail())
    }

    @ExceptionHandler(InternalException::class)
    fun handleInternal(ex: InternalException, exchange: ServerWebExchange): Mono<ProblemDetail> {
        log.error("Internal error", ex)
        exchange.response.statusCode = HttpStatus.valueOf(ex.httpStatus.value())
        exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON
        return Mono.just(ex.toProblemDetail())
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleBindException(ex: WebExchangeBindException, exchange: ServerWebExchange): Mono<ProblemDetail> {
        log.error("Validation error (WebExchangeBind)", ex)
        val detail = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        exchange.response.statusCode = HttpStatus.valueOf(400)
        exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON
        val pd = ProblemDetail.builder("Bad Request", 400)
            .type("/errors/validation-error")
            .detail(detail)
            .build()
        return Mono.just(pd)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception, exchange: ServerWebExchange): Mono<ProblemDetail> {
        log.error("Unhandled exception", ex)
        exchange.response.statusCode = HttpStatus.valueOf(500)
        exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON
        val pd = ProblemDetail.builder("Internal Server Error", 500)
            .detail("Internal Server Error")
            .build()
        return Mono.just(pd)
    }
}
