package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.enum.EntityType
import com.postraves.backend.postraveswiki.data.enum.FollowersType
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.context.annotation.Lazy

sealed interface QuickFollowersRepo {
    fun getFollowers(entityId: Long): Int
    fun setInitialFollowers(entityId: Long)
    fun incrementFollowers(entityId: Long): Int
    fun decrementFollowers(entityId: Long): Int
    fun findTop(stopValue: Long): Map<Long, Int>
    fun returnAllValuesToInitial()
    fun removeId(entityId: Long)
}

abstract class QuickFollowersRepoAbstract(
    private val entityType: String,
    private val followersType: String
) : QuickFollowersRepo {

    @Autowired @Lazy
    private lateinit var redisClient: RedisAsyncCommands<String, String>

    override fun getFollowers(entityId: Long): Int {
        val delta = redisClient.zscore("$entityType:$followersType", entityId.toString())
        val deltaResolved = delta.get()?.toInt()
        return if (deltaResolved == null) {
            setInitialFollowers(entityId)
            0
        } else deltaResolved
    }

    override fun setInitialFollowers(entityId: Long) {
        redisClient.zadd("$entityType:$followersType", 0.0, entityId.toString())
    }

    override fun incrementFollowers(entityId: Long): Int {
        val incrementedValue = redisClient.zincrby("$entityType:$followersType", 1.0, entityId.toString())
        return incrementedValue.get().toInt()
    }

    override fun decrementFollowers(entityId: Long): Int {
        val decrementValue = redisClient.zincrby("$entityType:$followersType", -1.0, entityId.toString())
        return decrementValue.get().toInt()
    }

    override fun findTop(stopValue: Long): Map<Long, Int> {
        val topFuture = redisClient.zrevrangeWithScores("$entityType:$followersType", 0, stopValue)
        val top = topFuture.get()
        return top.associate { it.value.toLong() to it.score.toInt() }
    }

    override fun returnAllValuesToInitial() {
        val list = redisClient.zrange("$entityType:$followersType", 0, -1).get()
        redisClient.multi()
        list.forEach { setInitialFollowers(it.toLong()) }
        redisClient.exec()
    }

    override fun removeId(entityId: Long) {
        redisClient.zrem("$entityType:$followersType", entityId.toString())
    }
}

abstract class WeeklyQuickFollowersRepo(entityType: String) : QuickFollowersRepoAbstract(entityType, FollowersType.WEEKLY.nameString)
abstract class OverallQuickFollowersRepo(entityType: String) : QuickFollowersRepoAbstract(entityType, FollowersType.OVERALL.nameString)

@Repository
class ArtistWeeklyQuickFollowersRepoImpl : WeeklyQuickFollowersRepo(EntityType.ARTIST.nameString)

@Repository
class UnityWeeklyQuickFollowersRepoImpl : WeeklyQuickFollowersRepo(EntityType.UNITY.nameString)

@Repository
class ArtistOverallQuickFollowersRepoImpl: OverallQuickFollowersRepo(EntityType.ARTIST.nameString)

@Repository
class UnityOverallQuickFollowersRepoImpl: OverallQuickFollowersRepo(EntityType.UNITY.nameString)

