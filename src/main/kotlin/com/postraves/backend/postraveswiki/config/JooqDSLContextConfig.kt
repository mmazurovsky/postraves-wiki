package com.postraves.backend.postraveswiki.config

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.DriverManager

@Component
class JooqDSLContextConfig(
    @Value("\${spring.datasource.url}")
    private val url: String? = null,
    @Value("\${spring.datasource.username}")
    private val username: String? = null,
    @Value("\${spring.datasource.password}")
    private val password: String? = null
) {
    private val dialect: SQLDialect = SQLDialect.POSTGRES
    private val connection: Connection = DriverManager.getConnection(url, username, password)
    private val dslContext = DSL.using(connection, dialect)

    @Bean
    fun getDSLContext(): DSLContext {
        return dslContext
    }
}