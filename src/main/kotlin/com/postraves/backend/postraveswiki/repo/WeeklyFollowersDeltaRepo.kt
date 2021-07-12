package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.config.RedisConfig

class WeeklyFollowersDeltaRepoImpl(
    val redisConfig: RedisConfig
) {

    fun getWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val delta = redisConfig.getRedisClient().hget("$entityType:$entityId", "weeklyFollowersDelta")
        return if (delta.get() == null)
            setInitialWeeklyFollowersDelta(entityType, entityId)
        else delta.get().toInt()
    }

    fun incrementWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val incrementedValue = redisConfig.getRedisClient().hincrby("$entityType:$entityId", "weeklyFollowersDelta", 1)
        return incrementedValue.get().toInt()
    }

    fun decrementWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val decrementValue = redisConfig.getRedisClient().hincrby("$entityType:$entityId", "weeklyFollowersDelta", -1)
        return decrementValue.get().toInt()
    }

    private fun setInitialWeeklyFollowersDelta(entityType: String, entityId: Long) : Int {
        val initValue = redisConfig.getRedisClient().hset("$entityType:$entityId", "weeklyFollowersDelta", "0")
        if (initValue.get()) return 0 else throw TODO()
    }


}