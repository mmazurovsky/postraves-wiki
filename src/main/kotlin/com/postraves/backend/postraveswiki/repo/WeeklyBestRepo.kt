package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.RedisConfig
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.stereotype.Repository

interface WeeklyBestRepo {
    fun setWeeklyBest(entityType: String, countryName: String, entity: Map<String, String>)
    fun getWeeklyBest(entityType: String, countryName: String): Map<String, String>
}

@Repository
class WeeklyBestRepoImpl(
    private val redisConfig: RedisConfig,
) : WeeklyBestRepo {
    private val redisClient: RedisAsyncCommands<String, String> by lazy { redisConfig.getRedisClient() }

    override fun setWeeklyBest(entityType: String, countryName: String, entity: Map<String, String>) {
        redisClient.hset("$entityType:$countryName:weeklyBest", entity)
    }

    override fun getWeeklyBest(entityType: String, countryName: String) : Map<String, String> {
        return redisClient.hgetall("$entityType:$countryName:weeklyBest").get()
    }
}