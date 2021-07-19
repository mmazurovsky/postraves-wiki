package com.postraves.backend.postraveswiki.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import lombok.RequiredArgsConstructor
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
@RequiredArgsConstructor
class SecurityConfig(
    private val objectMapper: ObjectMapper? = null,
//    private val restSecProps: SecurityProperties? = null,
    private val tokenAuthenticationFilter: SecurityFilter? = null
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun restAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint { httpServletRequest: HttpServletRequest?, httpServletResponse: HttpServletResponse, e: AuthenticationException? ->
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
            httpServletResponse.writer.write(objectMapper!!.writeValueAsString(errorObject))
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
//        configuration.allowedOrigins = restSecProps!!.allowedOrigins
//        configuration.allowedMethods = restSecProps.allowedMethods
//        configuration.allowedHeaders = restSecProps.allowedHeaders
//        configuration.allowCredentials = restSecProps.allowCredentials
//        configuration.exposedHeaders = restSecProps.exposedHeaders
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors().configurationSource(corsConfigurationSource()).and().csrf().disable().formLogin().disable()
            .httpBasic().disable().exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint())
            .and().authorizeRequests()
            .anyRequest().permitAll()
//            .antMatchers(*restSecProps!!.allowedPublicApis.toList()).permitAll()
//            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll().anyRequest().authenticated()
            .and()
//            .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
}