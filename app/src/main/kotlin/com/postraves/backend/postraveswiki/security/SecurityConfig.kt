package com.postraves.backend.postraveswiki.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.web.AuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.cors.CorsConfiguration
import kotlin.Throws
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.lang.Exception
import java.sql.Timestamp
import java.util.*

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class SecurityConfig(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val tokenAuthenticationFilter: SecurityFilter
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun restAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint { _: HttpServletRequest?, httpServletResponse: HttpServletResponse, e: AuthenticationException? ->
            // todo if there is exception on the server side and it is not connected with authentication,
            //  it also returns the error described below
            val errorObject: MutableMap<String, Any> = HashMap()
            val errorCode = 401
            errorObject["message"] = "Unauthorized access of protected resource, invalid credentials"
            errorObject["error"] = HttpStatus.UNAUTHORIZED
            errorObject["code"] = errorCode
            errorObject["timestamp"] = Timestamp(Date().time)
            httpServletResponse.contentType = "application/json;charset=UTF-8"
            httpServletResponse.status = errorCode
            httpServletResponse.writer.write(objectMapper.writeValueAsString(errorObject))
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOriginPatterns = listOf("https://*.postraves.com", "http://localhost:49257", "https://localhost:49257")
        config.allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        config.applyPermitDefaultValues()
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    override fun configure(http: HttpSecurity) {
        http.cors().configurationSource(corsConfigurationSource()).and().csrf().disable().formLogin().disable()
            .httpBasic().disable().exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint())
            .and()
            .authorizeRequests()
            .antMatchers("/**/public/**").permitAll()
            .antMatchers("/**/actuator/**").permitAll()
            .antMatchers("/**").authenticated()
            .and()
            .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
}