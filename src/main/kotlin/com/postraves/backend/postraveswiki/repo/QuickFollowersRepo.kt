package com.postraves.backend.postraveswiki.repo

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

sealed class QuickFollowersRepoImpl(
) : QuickFollowersRepo {
//    by lazy { redisConfig.getRedisClient() }

    @Autowired @Lazy
    private lateinit var redisClient: RedisAsyncCommands<String, String>

    abstract class WeeklyQuickFollowersDeltaRepo : QuickFollowersRepoImpl()
    @Repository
    class ArtistWeeklyQuickFollowersDeltaRepoImpl : WeeklyQuickFollowersDeltaRepo()
    @Repository
    class UnityWeeklyQuickFollowersDeltaRepoImpl : WeeklyQuickFollowersDeltaRepo()
    abstract class OverallQuickFollowersRepo : QuickFollowersRepoImpl()
    @Repository
    class ArtistOverallQuickFollowersRepoImpl: OverallQuickFollowersRepo()
    @Repository
    class UnityOverallQuickFollowersRepoImpl: OverallQuickFollowersRepo()

    private fun resolveFollowersType(): String {
        return when (this) {
            is WeeklyQuickFollowersDeltaRepo -> "weeklyFollowersDelta"
            is OverallQuickFollowersRepo -> "overallFollowersCount"
        }
    }

    private fun resolveEntityType(): String {
        return when (this) {
            is ArtistWeeklyQuickFollowersDeltaRepoImpl, is ArtistOverallQuickFollowersRepoImpl -> "artist"
            is UnityWeeklyQuickFollowersDeltaRepoImpl, is UnityOverallQuickFollowersRepoImpl -> "unity"
            else -> throw TODO()
        }
    }

    override fun getFollowers(entityId: Long): Int {
        val entityType = resolveEntityType()
        val followersType = resolveFollowersType()

        val delta = redisClient.zscore("$entityType:$followersType", entityId.toString())
        val deltaResolved = delta.get()?.toInt()
        return if (deltaResolved == null) {
            setInitialFollowers(entityId)
            0
        } else deltaResolved
    }

    override fun setInitialFollowers(entityId: Long) {
        val entityType = resolveEntityType()
        val followersType = resolveFollowersType()
        redisClient.zadd("$entityType:$followersType", 0.0, entityId.toString())
    }

    override fun incrementFollowers(entityId: Long): Int {
        val entityType = resolveEntityType()
        val followersType = resolveFollowersType()

        val incrementedValue = redisClient.zincrby("$entityType:$followersType", 1.0, entityId.toString())
        return incrementedValue.get().toInt()
    }

    override fun decrementFollowers(entityId: Long): Int {
        val entityType = resolveEntityType()
        val followersType = resolveFollowersType()

        val decrementValue = redisClient.zincrby("$entityType:$followersType", -1.0, entityId.toString())
        return decrementValue.get().toInt()
    }

    override fun findTop(stopValue: Long): Map<Long, Int> {
        val entityType = resolveEntityType()
        val followersType = resolveFollowersType()

        val topFuture = redisClient.zrevrangeWithScores("$entityType:$followersType", 0, stopValue)
        val top = topFuture.get()
        return top.associate { it.value.toLong() to it.score.toInt() }
    }

    override fun returnAllValuesToInitial() {
        val entityType = resolveEntityType()
        val followersType = resolveFollowersType()

        val list = redisClient.zrange("$entityType:$followersType", 0, -1).get()
        redisClient.multi()
        list.forEach { setInitialFollowers(it.toLong()) }
        redisClient.exec()
    }

    override fun removeId(entityId: Long) {
        val entityType = resolveEntityType()
        val followersType = resolveFollowersType()

        redisClient.zrem("$entityType:$followersType", entityId.toString())
    }
}
