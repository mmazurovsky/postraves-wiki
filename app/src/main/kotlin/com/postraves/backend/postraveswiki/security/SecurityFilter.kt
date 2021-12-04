package com.postraves.backend.postraveswiki.security

import lombok.extern.slf4j.Slf4j
import org.springframework.web.filter.OncePerRequestFilter
import com.postraves.backend.postraveswiki.service.followable.MyUserProfileService
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
import org.springframework.util.StringUtils
import java.io.IOException

@Component
@Slf4j
class SecurityFilter(
    private val cookieUtils: CookieUtils? = null,
    private val securityProps: SecurityProperties? = null,
    private val myUserProfileService: MyUserProfileService
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
        var decodedTokenWithFirebaseCredentials: FirebaseToken? = null
        var type: CredentialType? = null

        val strictServerSessionEnabled = securityProps!!.firebaseProps!!.enableStrictServerSession
        val sessionCookie = cookieUtils!!.getCookie("session")
        val token = getBearerToken(request)

        if (token == null || token.isEmpty() || token.equals("undefined", ignoreCase = true)) {
            return
        } else {
            try {
                if (sessionCookie != null) {
                    session = sessionCookie.value
                    decodedTokenWithFirebaseCredentials = FirebaseAuth.getInstance().verifySessionCookie(
                        session,
                        securityProps.firebaseProps!!.enableCheckSessionRevoked
                    )
                    type = CredentialType.SESSION
                } else if (!strictServerSessionEnabled) {
                    decodedTokenWithFirebaseCredentials = FirebaseAuth.getInstance().verifyIdToken(token)
                    type = CredentialType.ID_TOKEN
                }
            } catch (e: FirebaseAuthException) {
                logger.error("Firebase Exception: " + e.localizedMessage)
                logger.error("Firebase Exception stacktrace: " + e.printStackTrace())
            }

            if (decodedTokenWithFirebaseCredentials == null) {
                return
            }

            val userProfile = convertFirebaseTokenToMyBackendUser(decodedTokenWithFirebaseCredentials)
            logger.info("Token provided, userId is ${userProfile?.id}")

            val authentication =
                UsernamePasswordAuthenticationToken(
                    userProfile,
                    Credentials(type, decodedTokenWithFirebaseCredentials, token, session),
                    null
                )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        }
    }

    private fun convertFirebaseTokenToMyBackendUser(decodedToken: FirebaseToken): UserFullDto? {
        return myUserProfileService.getUserByAuthUidForSecurityService(decodedToken.uid)
    }

    private fun getBearerToken(request: HttpServletRequest): String? {
        var bearerToken: String? = null
        val authorization = request.getHeader("Authorization")
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            bearerToken = authorization.substring(7)
        }
        return bearerToken
    }
}