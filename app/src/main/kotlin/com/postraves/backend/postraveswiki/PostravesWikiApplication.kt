package com.postraves.backend.postraveswiki

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [SecurityAutoConfiguration::class]
)
class PostravesWikiApplication

fun main(args: Array<String>) {
    runApplication<PostravesWikiApplication>(*args)
}
