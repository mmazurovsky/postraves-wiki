package com.postraves.backend.postraveswiki.security

import com.postraves.backend.postraveswiki.security.dataclass.SecurityProperties
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.util.WebUtils
import javax.servlet.http.Cookie

@Service
class CookieUtils(
    private val httpServletRequest: HttpServletRequest? = null,
    private val httpServletResponse: HttpServletResponse? = null,
    private val restSecProps: SecurityProperties? = null
) {

    fun getCookie(name: String?): Cookie? {
        return WebUtils.getCookie(httpServletRequest!!, name!!)
    }

    fun setCookie(name: String?, value: String?, expiryInMinutes: Int) {
        val expiresInSeconds = expiryInMinutes * 60 * 60
        val cookie = Cookie(name, value)
        cookie.secure = restSecProps!!.cookieProps!!.secure
        cookie.path = restSecProps.cookieProps!!.path
        cookie.domain = restSecProps.cookieProps!!.domain
        cookie.maxAge = expiresInSeconds
        httpServletResponse!!.addCookie(cookie)
    }

    fun setSecureCookie(name: String?, value: String?, expiryInMinutes: Int) {
        val expiresInSeconds = expiryInMinutes * 60 * 60
        val cookie = Cookie(name, value)
        cookie.isHttpOnly = restSecProps!!.cookieProps!!.httpOnly
        cookie.secure = restSecProps.cookieProps!!.secure
        cookie.path = restSecProps.cookieProps!!.path
        cookie.domain = restSecProps.cookieProps!!.domain
        cookie.maxAge = expiresInSeconds
        httpServletResponse!!.addCookie(cookie)
    }

    fun setSecureCookie(name: String?, value: String?) {
        val expiresInMinutes = restSecProps!!.cookieProps!!.maxAgeInMinutes
        setSecureCookie(name, value, expiresInMinutes)
    }

    fun deleteSecureCookie(name: String?) {
        val expiresInSeconds = 0
        val cookie = Cookie(name, null)
        cookie.isHttpOnly = restSecProps!!.cookieProps!!.httpOnly
        cookie.secure = restSecProps.cookieProps!!.secure
        cookie.path = restSecProps.cookieProps!!.path
        cookie.domain = restSecProps.cookieProps!!.domain
        cookie.maxAge = expiresInSeconds
        httpServletResponse!!.addCookie(cookie)
    }

    fun deleteCookie(name: String?) {
        val expiresInSeconds = 0
        val cookie = Cookie(name, null)
        cookie.path = restSecProps!!.cookieProps!!.path
        cookie.domain = restSecProps.cookieProps!!.domain
        cookie.maxAge = expiresInSeconds
        httpServletResponse!!.addCookie(cookie)
    }
}