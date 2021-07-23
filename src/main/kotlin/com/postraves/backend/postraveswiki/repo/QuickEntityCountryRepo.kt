package com.postraves.backend.postraveswiki.repo

import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.context.annotation.Lazy

interface QuickEntityCountryRepo {
    fun addOneIdToCountry(countryName: String, entityId: Long)
    fun removeOneIdFromSet(countryName: String, entityId: Long)
    fun getAllIdsByCountry(countryName: String) : Set<Long>
}

sealed class QuickEntityCountryRepoImpl(
) : QuickEntityCountryRepo {

    @Autowired @Lazy
    private lateinit var redisClient: RedisAsyncCommands<String, String>

    @Repository
    class ArtistCountryQuickRepoImpl : QuickEntityCountryRepoImpl()

    private fun resolveEntityType(): String {
        return when (this) {
            is ArtistCountryQuickRepoImpl -> "artist"
            else -> throw TODO()
        }
    }

    override fun addOneIdToCountry(countryName: String, entityId: Long) {
        val entityType = resolveEntityType()
        redisClient.sadd("$entityType:${countryName.lowercase()}", entityId.toString())
    }

    override fun removeOneIdFromSet(countryName: String, entityId: Long) {
        val entityType = resolveEntityType()
        val result = redisClient.srem("$entityType:${countryName.lowercase()}", entityId.toString())
        // todo this is experimental
        if (result.error != null) throw TODO()
    }

    override fun getAllIdsByCountry(countryName: String) : Set<Long> {
        val entityType = resolveEntityType()
        val result = redisClient.smembers("$entityType:${countryName.lowercase()}")
        val resultResolved = result.get()
        return resultResolved.map {it.toLong()}.toSet()
    }
}
