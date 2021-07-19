package com.postraves.backend.postraveswiki.security

import lombok.extern.slf4j.Slf4j
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.beans.factory.annotation.Autowired
import com.postraves.backend.postraveswiki.service.UserService
import kotlin.Throws
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterChain
import com.google.firebase.auth.FirebaseToken
import com.postraves.backend.postraveswiki.security.dataclass.CredentialType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.security.dataclass.Credentials
import com.postraves.backend.postraveswiki.security.dataclass.SecurityProperties
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.io.IOException

@Component
@Slf4j
class SecurityFilter(
    private val securityService: SecurityService? = null,
    private val restSecProps: SecurityProperties? = null,
    private val cookieUtils: CookieUtils? = null,
    private val securityProps: SecurityProperties? = null,
    private val userService: UserService? = null
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        verifyToken(request)
        filterChain.doFilter(request, response)
    }

    private fun verifyToken(request: HttpServletRequest) {
        var session: String? = null
        var decodedToken: FirebaseToken? = null
        var type: CredentialType? = null
        val strictServerSessionEnabled = securityProps!!.firebaseProps!!.enableStrictServerSession
        val sessionCookie = cookieUtils!!.getCookie("session")
        val token = securityService!!.getBearerToken(request)
        if (token == null || token.isEmpty()) {
//            SecurityFilter.log.info("Incoming token is not provided")
        } else {
//            SecurityFilter.log.info("Incoming token is provided")
        }
        try {
            if (sessionCookie != null) {
                session = sessionCookie.value
                decodedToken = FirebaseAuth.getInstance().verifySessionCookie(
                    session,
                    securityProps!!.firebaseProps!!.enableCheckSessionRevoked
                )
                type = CredentialType.SESSION
            } else if (!strictServerSessionEnabled) {
                if (token != null && !token.equals("undefined", ignoreCase = true)) {
                    decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
                    type = CredentialType.ID_TOKEN
                }
            }
        } catch (e: FirebaseAuthException) {
            e.printStackTrace()
//            SecurityFilter.log.error("Firebase Exception:: " + e.localizedMessage)
        }
        assert(decodedToken != null)
        val userProfile = firebaseTokenToUser(decodedToken)
        if (userProfile != null) {
            val authentication = UsernamePasswordAuthenticationToken(
                decodedToken!!.uid,
                Credentials(type, decodedToken, token, session), null
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        }
    }

    private fun firebaseTokenToUser(decodedToken: FirebaseToken?): UserFullDto? {
        return userService!!.findByAuthUid(decodedToken!!.uid)
    }
}