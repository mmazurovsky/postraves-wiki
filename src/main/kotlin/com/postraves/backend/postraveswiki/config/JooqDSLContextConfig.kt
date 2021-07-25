package com.postraves.backend.postraveswiki.config

import com.postraves.backend.postraveswiki.exception.PostgresInitializationException
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import java.sql.Connection
import java.sql.DriverManager

@Configuration
class JooqDSLContextConfig {

    @Value("\${spring.datasource.url}")
    private val url: String? = null
    @Value("\${spring.datasource.username}")
    private val username: String? = null
    @Value("\${spring.datasource.password}")
    private val password: String? = null
    private val dialect: SQLDialect = SQLDialect.POSTGRES

    @Lazy @Bean
    fun getDSLContext(): DSLContext {
        val connection: Connection
        try {
            connection = DriverManager.getConnection(url, username, password)
        } catch (e: Exception) {
            throw PostgresInitializationException()
        }
        return DSL.using(connection, dialect)
    }
}