package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.RedisConfig
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.stereotype.Repository

@Repository
class WeeklyFollowersDeltaRepoImpl(
    private val redisConfig: RedisConfig,
) {
    private val redisClient: RedisAsyncCommands<String, String> by lazy { redisConfig.getRedisClient() }

    fun getWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val delta = redisClient.zscore("$entityType:weeklyFollowersDelta", entityId.toString())
        // dont know if null check appropriate here
        return delta.get().toInt()
    }

    fun incrementWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val incrementedValue = redisClient.zincrby("$entityType:weeklyFollowersDelta", 1.0, entityId.toString())
        return incrementedValue.get().toInt()
    }

    fun decrementWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val decrementValue = redisClient.zincrby("$entityType:weeklyFollowersDelta", -1.0, entityId.toString())
        return decrementValue.get().toInt()
    }

    fun getTop(entityType: String, quantity: Long) : List<Map<Long, Int>> {
        val top = redisClient.zrevrangeWithScores("$entityType:weeklyFollowersDelta", 0, quantity-1)
        // todo i dont know what will be if not enough values
        return top.get().map { mapOf(it.value.toLong() to it.score.toInt()) }.toList()
    }

    fun setInitialWeeklyFollowersDelta(entityType: String, entityId: Long) {
        redisClient.zadd("$entityType:weeklyFollowersDelta", 0.0, entityId.toString())
    }

    fun clearAllData() {
        redisClient.flushall().get()
    }

    fun returnAllValuesToInitial(entityType: String) {
        val list = redisClient.zrange("$entityType:weeklyFollowersDelta", 0, -1).get()
        redisClient.multi()
        list.forEach { setInitialWeeklyFollowersDelta(entityType, it.toLong()) }
        redisClient.exec()

//        val membersOfSortedSet = redisConfig.getRedisClient().zrange("$entityType:weeklyFollowersDelta", 0, -1)
//        val list = membersOfSortedSet.get()
//        list.forEach { setInitialWeeklyFollowersDelta(entityType, it.toLong()) }

    }


}