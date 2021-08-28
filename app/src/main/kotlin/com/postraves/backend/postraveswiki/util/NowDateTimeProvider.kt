package com.postraves.backend.postraveswiki.util

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

interface DateTimeProvider {
    fun getNow(): OffsetDateTime
}

@Service
@Scope("singleton")
class DateTimeProviderImpl private constructor() : DateTimeProvider {

    override fun getNow(): OffsetDateTime {
        return OffsetDateTime.now(UTC)
    }
}
