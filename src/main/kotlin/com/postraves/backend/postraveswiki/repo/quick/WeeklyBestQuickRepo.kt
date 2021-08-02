package com.postraves.backend.postraveswiki.repo.quick

import com.postraves.backend.postraveswiki.data.enum.EntityType
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface WeeklyBestQuickRepo {
    fun setWeeklyBestInCountry(countryName: String, entity: Map<String, String>)
    fun getWeeklyBestInCountry(countryName: String): Map<String, String>?
}

abstract class WeeklyBestQuickRepoAbstract(
    private val entityType: String
) : WeeklyBestQuickRepo {

    @Autowired
    @Lazy
    private lateinit var redisClient: RedisAsyncCommands<String, String>

    override fun setWeeklyBestInCountry(countryName: String, entity: Map<String, String>) {
        redisClient.hmset("$entityType:${countryName.lowercase()}:weeklyBest", entity)
    }

    override fun getWeeklyBestInCountry(countryName: String): Map<String, String>? {
        val weeklyBestInCountry = redisClient.hgetall("$entityType:${countryName.lowercase()}:weeklyBest").get()
        return if (weeklyBestInCountry.isEmpty()) null else weeklyBestInCountry
    }
}

@Repository
class ArtistWeeklyBestQuickRepoImpl : WeeklyBestQuickRepoAbstract(EntityType.ARTIST.nameString)

@Repository
class PlaceWeeklyBestQuickRepoImpl : WeeklyBestQuickRepoAbstract(EntityType.PLACE.nameString)

@Repository
class UnityWeeklyBestQuickRepoImpl : WeeklyBestQuickRepoAbstract(EntityType.UNITY.nameString)

