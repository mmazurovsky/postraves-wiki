package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.ArtistService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.utils.Requests
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import redis.embedded.RedisServer
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtistRatingIntegrationTest(
    @Autowired
    private val artistService: ArtistService,
    @Autowired
    private val countryService: CountryService,
    @Autowired
    private val mockMvc: MockMvc,
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
) : AbstractPostgresTest() {

    @MockBean
    private lateinit var securityService: SecurityService

    private val artistEndpoint: String = "/artist"
    private val redisServer = RedisServer(redisPort)

    init {
        redisServer.start()
    }

    @BeforeAll
    private fun createCountryAndCityForAssociations() {

        val country1 = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val city1 = CityWriteDto(
            name = "Brg",
            countryName = "BE",
            timeOffset = 3
        )

        val countryJson1 = Json.encodeToString(country1)
        val cityJson1 = Json.encodeToString(city1)

        Requests.makePostRequest(mockMvc, "/country", countryJson1, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, "/city", cityJson1, MockMvcResultMatchers.status().isCreated)
    }

    @AfterEach
    private fun cleanDb() = artistService.findAll().forEach { artistService.deleteById(it.id) }

    @AfterAll
    private fun cleanUp() {
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    fun saveArtistsAndIncrementFollowersAndFindOverallRating() {
        val artist1 = ArtistWriteDto(
            id = null,
            name = "Artist1",
            imageLink = "image1",
            countryName = "BE",
            about = "About1",
            instagramLink = "instagram1",
            soundcloudLink = "soundcloud1",
        )

        val artist2 = ArtistWriteDto(
            id = null,
            name = "Artist2",
            imageLink = "image2",
            countryName = "BE",
            about = "About2",
            instagramLink = "instagram2",
            soundcloudLink = "soundcloud2",
        )

        val artist3 = ArtistWriteDto(
            id = null,
            name = "Artist3",
            imageLink = "image3",
            countryName = "BE",
            about = "About3",
            instagramLink = "instagram3",
            soundcloudLink = "soundcloud3",
        )

        val artist4 = ArtistWriteDto(
            id = null,
            name = "Artist4",
            imageLink = "image4",
            countryName = "BE",
            about = "About4",
            instagramLink = "instagram4",
            soundcloudLink = "soundcloud4",
        )

        val response1 = Requests.makePostRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist1),
            MockMvcResultMatchers.status().isCreated
        )
        val response2 = Requests.makePostRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist2),
            MockMvcResultMatchers.status().isCreated
        )
        val response3 = Requests.makePostRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist3),
            MockMvcResultMatchers.status().isCreated
        )
        val response4 = Requests.makePostRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist4),
            MockMvcResultMatchers.status().isCreated
        )

        val artist1Id = Json.decodeFromString<ArtistShortDto>(response1).id
        val artist2Id = Json.decodeFromString<ArtistShortDto>(response2).id
        val artist3Id = Json.decodeFromString<ArtistShortDto>(response3).id
        Json.decodeFromString<ArtistShortDto>(response4).id


        Mockito.`when`(securityService.userAuthUid).thenReturn("abc")

        artistService.incrementFollowers(artist1Id)
        artistService.incrementFollowers(artist1Id)
        artistService.incrementFollowers(artist1Id)

        artistService.incrementFollowers(artist2Id)
        artistService.incrementFollowers(artist2Id)

        artistService.incrementFollowers(artist3Id)


        val responseOverallRatingJson =
            Requests.makeGetRequest(
                mockMvc,
                "/artist/public/overallRating?cityName=Brg&maxQuantity=10",
                MockMvcResultMatchers.status().isOk
            )
        val responseOverallRating = Json.decodeFromString<List<ArtistShortDto>>(responseOverallRatingJson)

        assertEquals(4, responseOverallRating.size)
        responseOverallRating.forEachIndexed { index, artistShortDto ->
            when (index) {
                0 -> {
                    assertEquals(artist1.name, artistShortDto.name)
                    assertEquals(3, artistShortDto.overallFollowers)
                    assertEquals(3, artistShortDto.weeklyFollowers)
                }
                1 -> {
                    assertEquals(artist2.name, artistShortDto.name)
                    assertEquals(2, artistShortDto.overallFollowers)
                    assertEquals(2, artistShortDto.weeklyFollowers)
                }
                2 -> {
                    assertEquals(artist3.name, artistShortDto.name)
                    assertEquals(1, artistShortDto.overallFollowers)
                    assertEquals(1, artistShortDto.weeklyFollowers)
                }
                3 -> {
                    assertEquals(artist4.name, artistShortDto.name)
                    assertEquals(0, artistShortDto.overallFollowers)
                    assertEquals(0, artistShortDto.weeklyFollowers)
                }
            }
        }
    }

    // todo weekly rating test

    @Test
    fun saveArtistsAndSetWeeklyBestAndGetWeeklyBest() {
        val artist1 = ArtistWriteDto(
            id = null,
            name = "Artist1",
            imageLink = "image1",
            countryName = "BE",
            about = "About1",
            instagramLink = "instagram1",
            soundcloudLink = "soundcloud1",
        )

        val artist2 = ArtistWriteDto(
            id = null,
            name = "Artist2",
            imageLink = "image2",
            countryName = "BE",
            about = "About2",
            instagramLink = "instagram2",
            soundcloudLink = "soundcloud2",
        )

        val artist3 = ArtistWriteDto(
            id = null,
            name = "Artist3",
            imageLink = "image3",
            countryName = "BE",
            about = "About3",
            instagramLink = "instagram3",
            soundcloudLink = "soundcloud3",
        )

        val artist4 = ArtistWriteDto(
            id = null,
            name = "Artist4",
            imageLink = "image4",
            countryName = "BE",
            about = "About4",
            instagramLink = "instagram4",
            soundcloudLink = "soundcloud4",
        )

        val response1 = Requests.makePostRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist1),
            MockMvcResultMatchers.status().isCreated
        )
        val response2 = Requests.makePostRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist2),
            MockMvcResultMatchers.status().isCreated
        )
        val response3 = Requests.makePostRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist3),
            MockMvcResultMatchers.status().isCreated
        )
        val response4 = Requests.makePostRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist4),
            MockMvcResultMatchers.status().isCreated
        )

        val artist1Id = Json.decodeFromString<ArtistShortDto>(response1).id
        val artist2Id = Json.decodeFromString<ArtistShortDto>(response2).id
        val artist3Id = Json.decodeFromString<ArtistShortDto>(response3).id
        Json.decodeFromString<ArtistShortDto>(response4).id


        Mockito.`when`(securityService.userAuthUid).thenReturn("abc")

        artistService.incrementFollowers(artist1Id)
        artistService.incrementFollowers(artist1Id)
        artistService.incrementFollowers(artist1Id)

        artistService.incrementFollowers(artist2Id)
        artistService.incrementFollowers(artist2Id)

        artistService.incrementFollowers(artist3Id)

        artistService.setBestOfTheWeekForAllCities()
        Thread.sleep(2_000)
        val artistOfTheWeek = artistService.findBestOfTheWeekByCityInCountry("Brg")

        assertEquals(artist1Id, artistOfTheWeek.id)
        assertEquals(artist1.name, artistOfTheWeek.name)
        assertEquals(artist1.countryName, artistOfTheWeek.country!!.name)
    }
}
