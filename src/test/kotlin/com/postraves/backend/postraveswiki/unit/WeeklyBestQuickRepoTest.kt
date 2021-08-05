package com.postraves.backend.postraveswiki.unit

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.repo.quick.CleaningQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.WeeklyBestQuickRepo
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import redis.embedded.RedisServer
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles(value = ["test"])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = ["spring.flyway.enabled=false"])
class WeeklyBestQuickRepoTest(
    @Value("\${spring.redis.port}")
    redisPort: Int,
    @Autowired
    private val quickRepoCleaning: CleaningQuickRepo,
    @Autowired @Qualifier("artistWeeklyBestQuickRepoImpl")
    private val weeklyBestQuickRepoTest: WeeklyBestQuickRepo,
) {

    private val redisServer = RedisServer(redisPort)

    init {
        redisServer.start()
    }

    companion object {
        private const val entityType = "artist"
        private const val countryName = "be"
        private const val defaultEntityId: Long = 1
    }

    @BeforeAll
    private fun clearAllData() {
        quickRepoCleaning.clearAllData()
    }

    @AfterAll
    private fun afterAll() {
        quickRepoCleaning.clearAllData()
        redisServer.stop()
    }

    @Test
    fun setBestAndGet() {
        val countryToSave = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val artistTestData = ArtistShortDto(
            id = 1,
            name = "Amelie Lens",
            imageLink = "image",
            country = countryToSave,
            weeklyFollowers = 11,
            overallFollowers = 200
        )

        weeklyBestQuickRepoTest.setWeeklyBestInCountry(countryToSave.name, artistTestData.toMap())
        val persistedMap = weeklyBestQuickRepoTest.getWeeklyBestInCountry(countryToSave.name)

        val persistedArtist = ArtistShortDto.fromMap(persistedMap!!)

        assertEquals(artistTestData, persistedArtist)
    }
}