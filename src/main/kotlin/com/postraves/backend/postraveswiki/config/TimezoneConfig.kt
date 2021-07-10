package com.postraves.backend.postraveswiki.config

import org.springframework.context.annotation.Configuration
import java.util.*
import javax.annotation.PostConstruct

@Configuration
class TimezoneConfig {
    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }
}
