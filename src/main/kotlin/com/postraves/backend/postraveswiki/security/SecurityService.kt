package com.postraves.backend.postraveswiki.security

import com.google.auth.Credentials
import com.postraves.backend.postraveswiki.security.dataclass.SecurityProperties
import lombok.RequiredArgsConstructor
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import javax.servlet.http.HttpServletRequest

@Service
@RequiredArgsConstructor
class SecurityService(
    private val httpServletRequest: HttpServletRequest? = null,
    private val cookieUtils: CookieUtils? = null,
    private val securityProps: SecurityProperties? = null
) {

    val userAuthUid: String?
        get() {
            var userPrincipal: String? = null
            val securityContext = SecurityContextHolder.getContext()
            val principal = securityContext.authentication.principal
            if (principal is String) {
                userPrincipal = principal
            }
            return userPrincipal
        }
    val credentials: Credentials
        get() {
            val securityContext = SecurityContextHolder.getContext()
            return securityContext.authentication.credentials as Credentials
        }
    val isPublic: Boolean
        get() = securityProps!!.allowedPublicApis!!.contains(httpServletRequest!!.requestURI)

    fun getBearerToken(request: HttpServletRequest): String? {
        var bearerToken: String? = null
        val authorization = request.getHeader("Authorization")
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            bearerToken = authorization.substring(7)
        }
        return bearerToken
    }
}