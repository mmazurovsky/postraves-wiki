package com.postraves.backend.postraveswiki.config

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.codec.RedisCodec
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


@Component
class RedisConfig(
    @Value("\${spring.redis.host}")
    redisHost: String? = null,
//    @Value("\${spring.redis.sentinel.master}")
//    redisMaster: String? = null,
    @Value("\${spring.redis.port}")
    redisPort: Int? = null,
) {
//    private val redisStandaloneConfig = RedisStandaloneConfiguration(redisHost ?: throw TODO(), redisPort ?: throw TODO())
//    private val lettuceConnectionFactory = LettuceConnectionFactory(redisStandaloneConfig)
//    private val redisTemplate = RedisTemplate<String, Int>()
    private val client: RedisClient = RedisClient
    .create(RedisURI.Builder.redis(redisHost ?: throw TODO(), redisPort ?: throw TODO()).build())
    private lateinit var connection: StatefulRedisConnection<String, String>

    fun getRedisClient(): RedisAsyncCommands<String, String> {
        if (!::connection.isInitialized) connection = client.connect()
        return connection.async()
    }
}