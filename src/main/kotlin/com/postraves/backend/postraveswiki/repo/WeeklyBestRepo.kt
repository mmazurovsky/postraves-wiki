package com.postraves.backend.postraveswiki.repo

import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.context.annotation.Lazy

interface WeeklyBestRepo {
    fun setWeeklyBestInCountry(countryName: String, entity: Map<String, String>)
    fun getWeeklyBestInCountry(countryName: String): Map<String, String>
}

sealed class WeeklyBestRepoImpl(
) : WeeklyBestRepo {

    @Autowired @Lazy
    private lateinit var redisClient: RedisAsyncCommands<String, String>

    @Repository
    class ArtistWeeklyBestRepoImpl : WeeklyBestRepoImpl()

    private fun resolveEntityType(): String {
        return when (this) {
            is ArtistWeeklyBestRepoImpl -> "artist"
            else -> throw TODO()
        }
    }

    override fun setWeeklyBestInCountry(countryName: String, entity: Map<String, String>) {
        val entityType = resolveEntityType()
        redisClient.hset("$entityType:${countryName.lowercase()}:weeklyBest", entity)
    }

    override fun getWeeklyBestInCountry(countryName: String) : Map<String, String> {
        val entityType = resolveEntityType()
        return redisClient.hgetall("$entityType:${countryName.lowercase()}:weeklyBest").get()
    }
}