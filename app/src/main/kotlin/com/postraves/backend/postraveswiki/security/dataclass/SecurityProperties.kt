package com.postraves.backend.postraveswiki.security.dataclass

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("security")
data class SecurityProperties (
    var cookieProps: CookieProperties? = null,
    var firebaseProps: FirebaseProperties? = null,
    var allowCredentials: Boolean = false,
    var allowedOrigins: List<String>? = null,
    var allowedHeaders: List<String>? = null,
    var exposedHeaders: List<String>? = null,
)