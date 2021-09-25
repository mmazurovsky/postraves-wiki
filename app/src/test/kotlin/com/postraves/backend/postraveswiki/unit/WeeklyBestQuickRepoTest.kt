package com.postraves.backend.postraveswiki.unit

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.converters.ArtistConverters
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.repo.quick.CleaningQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.WeeklyBestQuickRepo
import com.postraves.backend.postraveswiki.service.CountryService
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import redis.embedded.RedisServer
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles(value = ["test"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestPropertySource(properties = ["spring.flyway.enabled=false"])
class WeeklyBestQuickRepoTest(
    @Value("\${spring.redis.port}")
    redisPort: Int,
    @Autowired
    private val quickRepoCleaning: CleaningQuickRepo,
    @Autowired @Qualifier("artistWeeklyBestQuickRepoImpl")
    private val weeklyBestQuickRepoTest: WeeklyBestQuickRepo,
    @Autowired
    private val artistConverters: ArtistConverters,
    @Autowired
    private val countryService: CountryService,
) : AbstractPostgresTest() {

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
        logger.info("Weekly Best Unit Test started")
        quickRepoCleaning.clearAllData()
    }

    @AfterAll
    private fun afterAll() {
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        quickRepoCleaning.clearAllData()
        redisServer.stop()
        logger.info("Weekly Best Unit Test ended")
    }

    @ExperimentalSerializationApi
    @Test
    fun setBestAndGet() {
        val countryToSave = CountryWriteDto(
            name = "BE",
            nameRu = "NameRu",
            nameEn = "NameUk",
            nameDe = "NameDe",
            nameFr = "NameFr",
            phoneCode = "+7",
        )

        val savedCountry = countryService.save(countryToSave)

        val artistTestData = ArtistShortDto(
            id = 1,
            name = "Amelie Lens",
            imageLink = "image",
            country = savedCountry,
            weeklyFollowers = 11,
            overallFollowers = 200
        )

        weeklyBestQuickRepoTest.setWeeklyBestInCountry(countryToSave.name, artistTestData.toMap())
        val persistedMap = weeklyBestQuickRepoTest.getWeeklyBestInCountry(countryToSave.name)

        val persistedArtist = artistConverters.createShortDtoFromMap(persistedMap!!)

        assertEquals(artistTestData, persistedArtist)
    }
}