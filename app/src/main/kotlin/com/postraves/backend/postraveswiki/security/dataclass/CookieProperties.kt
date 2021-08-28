package com.postraves.backend.postraveswiki.security.dataclass

data class CookieProperties(
    var domain: String? = null,
    var path: String? = null,
    var httpOnly: Boolean = false,
    var secure: Boolean = false,
    var maxAgeInMinutes: Int = 0
)