package com.postraves.backend.postraveswiki.security

import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.security.dataclass.Credentials
import com.postraves.backend.postraveswiki.security.dataclass.SecurityProperties
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import javax.servlet.http.HttpServletRequest

@Service
class SecurityService(
    private val httpServletRequest: HttpServletRequest? = null,
    private val cookieUtils: CookieUtils? = null,
    private val securityProps: SecurityProperties? = null
) {

    val user: UserFullDto?
        get() {
            val securityContext = SecurityContextHolder.getContext()
            val principal = securityContext.authentication?.principal //todo
            return if (principal == null || principal == "anonymousUser") {
                null
            } else {
                principal as UserFullDto
            }
        }

    val firebaseAuthUid: String?
        get() {
            return credentials?.decodedToken?.uid
        }


    val credentials: Credentials?
        get() {
            val securityContext = SecurityContextHolder.getContext()
            val rawCredentials = securityContext.authentication?.credentials
            return if (rawCredentials == "") null else rawCredentials as Credentials
        }

    fun getBearerToken(request: HttpServletRequest): String? {
        var bearerToken: String? = null
        val authorization = request.getHeader("Authorization")
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            bearerToken = authorization.substring(7)
        }
        return bearerToken
    }
}