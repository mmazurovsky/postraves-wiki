package com.postraves.backend.postraveswiki.controller.handler

import com.postraves.backend.postraveswiki.exception.AuthenticationException
import com.postraves.backend.postraveswiki.exception.BadRequestException
import com.postraves.backend.postraveswiki.exception.ServerInternalException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CollectorErrorHandler {
    @ExceptionHandler(ServerInternalException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun forServerInternalExceptions(e: ServerInternalException): String? {
        return e.message
    }

    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun forBadRequestExceptions(e: BadRequestException): String? {
        return e.message
    }

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun forBadRequestExceptions(e: AuthenticationException): String? {
        return e.message
    }
}