package com.postraves.backend.postraveswiki.controller.handler

import com.postraves.backend.postraveswiki.exception.InitializationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CollectorErrorHandler {
    @ExceptionHandler(InitializationException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun forServerExceptions(e: InitializationException): String? {
        return e.message
    }
}