package com.postraves.backend.postraveswiki.security

import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.security.dataclass.Credentials
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SecurityService {

    // INFO this user has country and city in local language that could be changed afterwards
    val user: UserFullDto?
        get() {
            val securityContext = SecurityContextHolder.getContext()
            val principal = securityContext.authentication?.principal
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
            return if (rawCredentials == "" || rawCredentials == null) null else rawCredentials as Credentials
        }
}