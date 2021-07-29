package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.followable.ArtistService
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
import kotlin.test.assertNull

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
            emojiCode = null
        )

        val country2 = CountryDto(
            name = "RU",
            phoneCode = "+9",
            emojiCode = null
        )

        val country3 = CountryDto(
            name = "CA",
            phoneCode = "+10",
            emojiCode = null
        )

        val city1 = CityWriteDto(
            name = "Bruges",
            countryName = "BE",
            timeOffset = 3
        )

        val city2 = CityWriteDto(
            name = "Moscow",
            countryName = "RU",
            timeOffset = 3
        )

        val city3 = CityWriteDto(
            name = "Toronto",
            countryName = "CA",
            timeOffset = 3
        )

        val countryJson1 = Json.encodeToString(country1)
        val countryJson2 = Json.encodeToString(country2)
        val countryJson3 = Json.encodeToString(country3)
        val cityJson1 = Json.encodeToString(city1)
        val cityJson2 = Json.encodeToString(city2)
        val cityJson3 = Json.encodeToString(city3)

        Requests.makePostRequest(mockMvc, "/country", countryJson1, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, "/country", countryJson2, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, "/country", countryJson3, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, "/city", cityJson1, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, "/city", cityJson2, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, "/city", cityJson3, MockMvcResultMatchers.status().isCreated)
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
                "/artist/public/overallRating?cityName=Bruges&maxQuantity=10",
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

    @Test
    fun saveArtistsAndIncrementFollowersAndFindWeeklyRating() {
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
        val artist4Id = Json.decodeFromString<ArtistShortDto>(response4).id


        Mockito.`when`(securityService.userAuthUid).thenReturn("abc")

        var i: Int = 0
        while (i < 3) {
            i++
            artistService.incrementFollowers(artist1Id)
        }
        i = 0
        while (i < 2) {
            i++
            artistService.incrementFollowers(artist2Id)
        }

        artistService.incrementFollowers(artist3Id)

        artistService.decrementFollowers(artist4Id)


        val responseWeeklyRatingJson =
            Requests.makeGetRequest(
                mockMvc,
                "/artist/public/weeklyRating?cityName=Bruges&maxQuantity=10",
                MockMvcResultMatchers.status().isOk
            )
        val responseWeeklyRating = Json.decodeFromString<List<ArtistShortDto>>(responseWeeklyRatingJson)

        assertEquals(4, responseWeeklyRating.size)
        responseWeeklyRating.forEachIndexed { index, artistShortDto ->
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
                    assertEquals(-1, artistShortDto.overallFollowers)
                    assertEquals(-1, artistShortDto.weeklyFollowers)
                }
            }
        }
    }

    @Test
    fun saveArtistsAndIncrementFollowersAndChangeCountriesOfArtistsAndGetOverallRating() {
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
        val artist4Id = Json.decodeFromString<ArtistShortDto>(response4).id


        Mockito.`when`(securityService.userAuthUid).thenReturn("abc")

        var i: Int = 0
        while (i < 3) {
            i++
            artistService.incrementFollowers(artist1Id)
        }
        i = 0
        while (i < 2) {
            i++
            artistService.incrementFollowers(artist2Id)
        }

        artistService.incrementFollowers(artist3Id)

        artistService.decrementFollowers(artist4Id)

        val artist1Updated = artist1
            .copy(
                id = artist1Id,
                countryName = "RU"
            )

        val artist2Updated = artist2
            .copy(
                id = artist2Id,
                countryName = "RU"
            )

        val artist3Updated = artist3
            .copy(
                id = artist3Id,
                countryName = "CA"
            )

        val artist4Updated = artist4
            .copy(
                id = artist4Id,
                countryName = null
            )

        Requests.makePutRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist1Updated),
            MockMvcResultMatchers.status().isOk
        )
        Requests.makePutRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist2Updated),
            MockMvcResultMatchers.status().isOk
        )
        Requests.makePutRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist3Updated),
            MockMvcResultMatchers.status().isOk
        )
        Requests.makePutRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist4Updated),
            MockMvcResultMatchers.status().isOk
        )

        val brugesOverallRatingJson =
            Requests.makeGetRequest(
                mockMvc,
                "/artist/public/overallRating?cityName=Bruges&maxQuantity=10",
                MockMvcResultMatchers.status().isOk
            )
        val moscowOverallRatingJson =
            Requests.makeGetRequest(
                mockMvc,
                "/artist/public/overallRating?cityName=Moscow&maxQuantity=10",
                MockMvcResultMatchers.status().isOk
            )
        val torontoOverallRatingJson =
            Requests.makeGetRequest(
                mockMvc,
                "/artist/public/overallRating?cityName=Toronto&maxQuantity=10",
                MockMvcResultMatchers.status().isOk
            )

        val brugesOverallRating = Json.decodeFromString<List<ArtistShortDto>>(brugesOverallRatingJson)
        val moscowOverallRating = Json.decodeFromString<List<ArtistShortDto>>(moscowOverallRatingJson)
        val torontoOverallRating = Json.decodeFromString<List<ArtistShortDto>>(torontoOverallRatingJson)

        assertEquals(0, brugesOverallRating.size)
        assertEquals(2, moscowOverallRating.size)
        assertEquals(1, torontoOverallRating.size)

        moscowOverallRating.forEachIndexed { index, artistShortDto ->
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
            }
        }
        torontoOverallRating.forEachIndexed { index, artistShortDto ->
            when (index) {
                0 -> {
                    assertEquals(artist3.name, artistShortDto.name)
                    assertEquals(1, artistShortDto.overallFollowers)
                    assertEquals(1, artistShortDto.weeklyFollowers)
                }
            }
        }
    }

    @Test
    fun saveArtistsAndIncrementFollowersAndChangeCountriesOfArtistsAndGetWeeklyRating() {
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
        val artist4Id = Json.decodeFromString<ArtistShortDto>(response4).id


        Mockito.`when`(securityService.userAuthUid).thenReturn("abc")

        var i: Int = 0
        while (i < 3) {
            i++
            artistService.incrementFollowers(artist1Id)
        }
        i = 0
        while (i < 2) {
            i++
            artistService.incrementFollowers(artist2Id)
        }

        artistService.incrementFollowers(artist3Id)

        artistService.decrementFollowers(artist4Id)

        val artist1Updated = artist1
            .copy(
                id = artist1Id,
                countryName = "RU"
            )

        val artist2Updated = artist2
            .copy(
                id = artist2Id,
                countryName = "RU"
            )

        val artist3Updated = artist3
            .copy(
                id = artist3Id,
                countryName = "CA"
            )

        val artist4Updated = artist4
            .copy(
                id = artist4Id,
                countryName = null
            )

        Requests.makePutRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist1Updated),
            MockMvcResultMatchers.status().isOk
        )
        Requests.makePutRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist2Updated),
            MockMvcResultMatchers.status().isOk
        )
        Requests.makePutRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist3Updated),
            MockMvcResultMatchers.status().isOk
        )
        Requests.makePutRequest(
            mockMvc,
            artistEndpoint,
            Json.encodeToString(artist4Updated),
            MockMvcResultMatchers.status().isOk
        )

        val brugesWeeklyRatingJson =
            Requests.makeGetRequest(
                mockMvc,
                "/artist/public/weeklyRating?cityName=Bruges&maxQuantity=10",
                MockMvcResultMatchers.status().isOk
            )
        val moscowWeeklyRatingJson =
            Requests.makeGetRequest(
                mockMvc,
                "/artist/public/weeklyRating?cityName=Moscow&maxQuantity=10",
                MockMvcResultMatchers.status().isOk
            )
        val torontoWeeklyRatingJson =
            Requests.makeGetRequest(
                mockMvc,
                "/artist/public/weeklyRating?cityName=Toronto&maxQuantity=10",
                MockMvcResultMatchers.status().isOk
            )

        val brugesWeeklyRating = Json.decodeFromString<List<ArtistShortDto>>(brugesWeeklyRatingJson)
        val moscowWeeklyRating = Json.decodeFromString<List<ArtistShortDto>>(moscowWeeklyRatingJson)
        val torontoWeeklyRating = Json.decodeFromString<List<ArtistShortDto>>(torontoWeeklyRatingJson)

        assertEquals(0, brugesWeeklyRating.size)
        assertEquals(2, moscowWeeklyRating.size)
        assertEquals(1, torontoWeeklyRating.size)

        moscowWeeklyRating.forEachIndexed { index, artistShortDto ->
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
            }
        }
        torontoWeeklyRating.forEachIndexed { index, artistShortDto ->
            when (index) {
                0 -> {
                    assertEquals(artist3.name, artistShortDto.name)
                    assertEquals(1, artistShortDto.overallFollowers)
                    assertEquals(1, artistShortDto.weeklyFollowers)
                }
            }
        }
    }

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
        val artistOfTheWeek = artistService.findBestOfTheWeekByCityInCountry("Bruges")

        assertEquals(artist1Id, artistOfTheWeek!!.id)
        assertEquals(artist1.name, artistOfTheWeek.name)
        assertEquals(artist1.countryName, artistOfTheWeek.country!!.name)
    }

    @Test
    fun getBestOfTheWeekWhenItIsNotSet() {
        val artistOfTheWeek = artistService.findBestOfTheWeekByCityInCountry("Bruges")
        assertNull(artistOfTheWeek)
    }
}
