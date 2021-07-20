package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.RedisConfig
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

interface WeeklyFollowersDeltaRepo {
    fun getWeeklyFollowersDelta(entityType: String, countryName: String, entityId: Long): Int
    fun incrementWeeklyFollowersDelta(entityType: String, countryName: String, entityId: Long): Int
    fun decrementWeeklyFollowersDelta(entityType: String, countryName: String, entityId: Long): Int
    fun findWeeklyTopInCountry(entityType: String, countryName: String, quantity: Long): Map<Long, Int>
    fun setInitialWeeklyFollowersDelta(entityType: String, countryName: String, entityId: Long)
    fun returnAllValuesToInitial(entityType: String, countryName: String)
}

@Repository
class WeeklyFollowersDeltaRepoImpl(
    private val redisConfig: RedisConfig,
) : WeeklyFollowersDeltaRepo {
    private val redisClient: RedisAsyncCommands<String, String> by lazy { redisConfig.getRedisClient() }

    override fun getWeeklyFollowersDelta(entityType: String, countryName: String, entityId: Long): Int {
        val delta = redisClient.zscore("$entityType:$countryName:weeklyFollowersDelta", entityId.toString())
        return delta.get().toInt()
    }

    override fun incrementWeeklyFollowersDelta(entityType: String, countryName: String, entityId: Long): Int {
        val incrementedValue = redisClient.zincrby("$entityType:$countryName:weeklyFollowersDelta", 1.0, entityId.toString())
        return incrementedValue.get().toInt()
    }

    override fun decrementWeeklyFollowersDelta(entityType: String, countryName: String, entityId: Long): Int {
        val decrementValue = redisClient.zincrby("$entityType:$countryName:weeklyFollowersDelta", -1.0, entityId.toString())
        return decrementValue.get().toInt()
    }

    override fun findWeeklyTopInCountry(entityType: String, countryName: String, quantity: Long): Map<Long, Int> {
        val topFuture = redisClient.zrevrangeWithScores("$entityType:$countryName:weeklyFollowersDelta", 0, quantity - 1)
        val top = topFuture.get()
        val map = top.map { it.value.toLong() to it.score.toInt() }.toMap()
        return map
    }

    override fun setInitialWeeklyFollowersDelta(entityType: String, countryName: String, entityId: Long) {
        redisClient.zadd("$entityType:$countryName:weeklyFollowersDelta", 0.0, entityId.toString())
    }

    fun clearAllData() {
        redisClient.flushall()
    }

    override fun returnAllValuesToInitial(entityType: String, countryName: String) {
        val list = redisClient.zrange("$entityType:$countryName:weeklyFollowersDelta", 0, -1).get()
        redisClient.multi()
        list.forEach { setInitialWeeklyFollowersDelta(entityType, countryName, it.toLong()) }
        redisClient.exec()

//        val membersOfSortedSet = redisConfig.getRedisClient().zrange("$entityType:weeklyFollowersDelta", 0, -1)
//        val list = membersOfSortedSet.get()
//        list.forEach { setInitialWeeklyFollowersDelta(entityType, it.toLong()) }

    }


}