package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.config.RedisConfig
import io.lettuce.core.api.async.multi

class WeeklyFollowersDeltaRepoImpl(
    val redisConfig: RedisConfig
) {

    fun getWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val delta = redisConfig.getRedisClient().zscore("$entityType:weeklyFollowersDelta", entityId.toString())
        // dont know if null check appropriate here
        return delta.get().toInt()
    }

    fun incrementWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val incrementedValue = redisConfig.getRedisClient().zincrby("$entityType:weeklyFollowersDelta", 1.0, entityId.toString())
        return incrementedValue.get().toInt()
    }

    fun decrementWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val decrementValue = redisConfig.getRedisClient().zincrby("$entityType:weeklyFollowersDelta", -1.0, entityId.toString())
        return decrementValue.get().toInt()
    }

    fun getTop(entityType: String, quantity: Long) : List<Map<String, Double>> {
        val top = redisConfig.getRedisClient().zrevrangeWithScores("$entityType:weeklyFollowersDelta", 0, quantity-1)
        // todo i dont know what will be if not enough values
        return top.get().map { mapOf(it.value to it.score) }.toList()
    }

    private fun setInitialWeeklyFollowersDelta(entityType: String, entityId: Long) {
        val initValue = redisConfig.getRedisClient().zadd("$entityType:weeklyFollowersDelta", 0, entityId.toString())
        initValue.get()
    }

    suspend fun returnAllValuesToInitial(entityType: String) {
        redisConfig.getRedisClient().multi {
            val list = zrange("$entityType:weeklyFollowersDelta", 0, -1).get()
            list.forEach { setInitialWeeklyFollowersDelta(entityType, it.toLong()) }
        }
//        val membersOfSortedSet = redisConfig.getRedisClient().zrange("$entityType:weeklyFollowersDelta", 0, -1)
//        val list = membersOfSortedSet.get()
//        list.forEach { setInitialWeeklyFollowersDelta(entityType, it.toLong()) }

    }


}