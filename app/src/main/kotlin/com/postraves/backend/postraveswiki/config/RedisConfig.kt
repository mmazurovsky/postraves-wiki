package com.postraves.backend.postraveswiki.config

import com.postraves.backend.postraveswiki.exception.RedisInitializationException
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy


@Configuration
class RedisConfig {

    @Value("\${spring.redis.host}")
    val redisHost: String? = null

    @Value("\${spring.redis.port}")
    val redisPort: Int? = null

    @Value("\${spring.redis.username}")
    val redisUsername: String? = null

    @Value("\${spring.redis.password}")
    val redisPassword: String? = null

    @Lazy
    @Bean
    fun getRedisClient(): RedisAsyncCommands<String, String> {
        val client: RedisClient = RedisClient
            .create(
                RedisURI.Builder.redis(
                    redisHost ?: throw RedisInitializationException(),
                ).withPort(
                    redisPort ?: throw RedisInitializationException(),
                ).withClientName(
                    redisUsername ?: throw RedisInitializationException(),
                ).withPassword(
                    redisPassword?.toCharArray() ?: throw RedisInitializationException(),
                ).withSsl(
                    true
                )
                    .build()
            )
        val connection = client.connect()
        return connection.async()
    }
}
