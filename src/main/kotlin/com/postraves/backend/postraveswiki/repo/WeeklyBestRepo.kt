package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.enum.EntityType
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.context.annotation.Lazy

interface WeeklyBestRepo {
    fun setWeeklyBestInCountry(countryName: String, entity: Map<String, String>)
    fun getWeeklyBestInCountry(countryName: String): Map<String, String>
}

abstract class WeeklyBestRepoAbstract(
    private val entityType: String
) : WeeklyBestRepo {

    @Autowired @Lazy
    private lateinit var redisClient: RedisAsyncCommands<String, String>

    override fun setWeeklyBestInCountry(countryName: String, entity: Map<String, String>) {
        redisClient.hmset("$entityType:${countryName.lowercase()}:weeklyBest", entity)
    }

    override fun getWeeklyBestInCountry(countryName: String) : Map<String, String> {
        return redisClient.hgetall("$entityType:${countryName.lowercase()}:weeklyBest").get()
    }
}

@Repository
class ArtistWeeklyBestRepoImpl : WeeklyBestRepoAbstract(EntityType.ARTIST.nameString)

@Repository
class PlaceWeeklyBestRepoImpl : WeeklyBestRepoAbstract(EntityType.PLACE.nameString)

