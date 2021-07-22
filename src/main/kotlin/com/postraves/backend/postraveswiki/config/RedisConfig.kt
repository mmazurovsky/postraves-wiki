package com.postraves.backend.postraveswiki.config

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component


//@Component
@Configuration
class RedisConfig(
) {
    @Value("\${spring.redis.host}")
    val redisHost: String? = null
    @Value("\${spring.redis.port}")
    val redisPort: Int? = null

    @Bean
    fun getRedisClient(): RedisAsyncCommands<String, String> {
        val client: RedisClient = RedisClient.create(RedisURI.Builder.redis(redisHost ?: throw TODO(), redisPort ?: throw TODO()).build())
        val connection = client.connect()
        return connection.async()
    }
}