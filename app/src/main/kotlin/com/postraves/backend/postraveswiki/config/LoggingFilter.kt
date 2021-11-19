package com.postraves.backend.postraveswiki.config

import org.springframework.stereotype.Component
import org.springframework.web.filter.AbstractRequestLoggingFilter
import javax.servlet.http.HttpServletRequest


@Component
class RequestLoggingFilter : AbstractRequestLoggingFilter() {
    private val excludedUrls = setOf("/api/actuator/health", "/actuator/health")
    override fun shouldLog(request: HttpServletRequest): Boolean {
        return if (excludedUrls.contains(request.requestURI)) {
            false
        } else logger.isDebugEnabled
    }

    override fun beforeRequest(request: HttpServletRequest, message: String) {
        logger.debug(message)
    }

    override fun afterRequest(request: HttpServletRequest, message: String) {
        logger.debug(message)
    }
}