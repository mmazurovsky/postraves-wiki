package com.postraves.backend.postraveswiki.repo.quick

import com.postraves.backend.postraveswiki.data.enum.EntityType
import com.postraves.backend.postraveswiki.exception.DeleteException
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.context.annotation.Lazy

interface EntityCountryQuickRepo {
    fun addOneIdToCountry(countryName: String, entityId: Long)
    fun removeOneIdFromSet(countryName: String, entityId: Long)
    fun getAllIdsByCountry(countryName: String) : Set<Long>
}

abstract class EntityCountryQuickRepoAbstract(
    private val entityType: String
) : EntityCountryQuickRepo {

    @Autowired @Lazy
    private lateinit var redisClient: RedisAsyncCommands<String, String>

    override fun addOneIdToCountry(countryName: String, entityId: Long) {
        redisClient.sadd("$entityType:${countryName.lowercase()}", entityId.toString())
    }

    override fun removeOneIdFromSet(countryName: String, entityId: Long) {
        val result = redisClient.srem("$entityType:${countryName.lowercase()}", entityId.toString())
        // todo this is experimental
        if (result.error != null) throw DeleteException("CountryQuickRepo country $countryName", entityType, entityId.toString())
    }

    override fun getAllIdsByCountry(countryName: String) : Set<Long> {
        val result = redisClient.smembers("$entityType:${countryName.lowercase()}")
        val resultResolved = result.get()
        return resultResolved.map {it.toLong()}.toSet()
    }
}

@Repository
class ArtistCountryQuickRepoImpl : EntityCountryQuickRepoAbstract(EntityType.ARTIST.nameString)

@Repository
class PlaceCountryQuickRepoImpl : EntityCountryQuickRepoAbstract(EntityType.PLACE.nameString)

@Repository
class UnityCountryQuickRepoImpl : EntityCountryQuickRepoAbstract(EntityType.UNITY.nameString)
