package com.postraves.backend.postraveswiki

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles(value = ["test"])
class PostravesWikiApplicationTests : AbstractPostgresTest() {

    @Test
    fun contextLoads() {
    }

}
