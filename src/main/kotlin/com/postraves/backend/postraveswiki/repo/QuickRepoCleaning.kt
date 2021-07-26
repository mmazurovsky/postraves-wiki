package com.postraves.backend.postraveswiki.repo

import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface QuickRepoCleaning {
    fun clearAllData()
}

@Repository
class QuickRepoCleaningImpl : QuickRepoCleaning {

    @Autowired
    @Lazy
    private lateinit var redisClient: RedisAsyncCommands<String, String>

    override fun clearAllData() {
        redisClient.flushall()
    }
}