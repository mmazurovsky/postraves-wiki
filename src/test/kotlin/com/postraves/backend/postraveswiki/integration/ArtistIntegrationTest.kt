package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.service.ArtistService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.utils.Requests
import com.postraves.backend.postraveswiki.utils.Requests.makeDeleteRequest
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePutRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import redis.embedded.RedisServer

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtistIntegrationTest (
    @Autowired private val artistService: ArtistService,
    @Autowired private val countryService: CountryService,
    @Autowired private val mockMvc: MockMvc,
    @Value("\${spring.redis.port}") redisPort: Int,
    ) : AbstractPostgresTest() {

    private val artistEndpoint: String = "/artist"

    private val redisServer = RedisServer(redisPort)
    init {
        redisServer.start()
    }

    @BeforeAll
    private fun createCountryForAssociations() {

        val country = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        makePostRequest(mockMvc, "/country", Json.encodeToString(country), status().isCreated)
    }

    @AfterEach
    private fun cleanDb() = artistService.findAll().forEach { artistService.deleteById(it.id) }

    @AfterAll
    private fun cleanUp() {
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }


    @Test
    fun saveArtistWithCountryAssociation() {

        val artistToSave = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudLink = "soundcloud",
            instagramLink = "instagram",
            about = "About Amelie",
            countryName = "BE",
            soundcloudFollowersCount = 100,
        )

        val artistIdRespJson = makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id

        val artistRespJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val artistDecoded = Json.decodeFromString<ArtistFullDto>(artistRespJson)

        assertNotNull(artistDecoded.id)
        assertEquals("Amelie Lens", artistDecoded.name)
        assertEquals(20, artistDecoded.baseRating)
        assertEquals(0, artistDecoded.overallFollowersCount)
        assertEquals("image", artistDecoded.imageLink)
        assertEquals("soundcloud", artistDecoded.soundcloudLink)
        assertEquals("instagram", artistDecoded.instagramLink)
        assertEquals("About Amelie", artistDecoded.about)
        assertEquals("BE", artistDecoded.country?.name)
        assertEquals("+7", artistDecoded.country?.phoneCode)
        assertEquals("EBE", artistDecoded.country?.emojiCode)
    }

    @Test
    fun updateArtistAndDeleteCountryAssociation() {

        val artistToSave = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudLink = "soundcloud",
            instagramLink = "instagram",
            about = "About Amelie",
            countryName = "BE",
            soundcloudFollowersCount = 100,
        )

        val responseSavedArtist =
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val savedId = Json.decodeFromString<ArtistShortDto>(responseSavedArtist).id

        val artistToUpdate = artistToSave.copy(
            id = savedId,
            name = "Amelie Lens2",
            imageLink = "image2",
            soundcloudLink = "soundcloud2",
            instagramLink = "instagram2",
            about = "About Amelie2",
            countryName = null,
            // this must not impact base rating
            soundcloudFollowersCount = 100000,
        )

        makePutRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToUpdate), status().isOk)

        val updatedJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$savedId", status().isOk)
        val updatedArtist = Json.decodeFromString<ArtistFullDto>(updatedJson)

        assertEquals(savedId, updatedArtist.id)
        assertEquals("Amelie Lens2", updatedArtist.name)
        assertEquals(20, updatedArtist.baseRating)
        assertEquals(0, updatedArtist.overallFollowersCount)
        assertEquals("image2", updatedArtist.imageLink)
        assertEquals("soundcloud2", updatedArtist.soundcloudLink)
        assertEquals("instagram2", updatedArtist.instagramLink)
        assertEquals("About Amelie2", updatedArtist.about)
        assertNull(updatedArtist.country)
    }

    @Test
    fun deleteArtistById() {

        val artistToSave = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudLink = "soundcloud",
            instagramLink = "instagram",
            about = "About Amelie",
            countryName = "BE",
            soundcloudFollowersCount = 100,
        )

        val responseSavedArtist = makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val savedId = Json.decodeFromString<ArtistShortDto>(responseSavedArtist).id

        makeDeleteRequest(mockMvc, "$artistEndpoint/$savedId", status().isOk)

        val responseFindArtistJson = makeGetRequest(mockMvc, artistEndpoint, status().isOk)
        val responseFindArtist = Json.decodeFromString<List<ArtistShortDto>>(responseFindArtistJson)

        assertEquals(0, responseFindArtist.size)
    }

    @Test
    fun saveMultipleArtistsWithoutCountriesAndFindAll() {
        val artist1 = ArtistWriteDto(
            id = null,
            name = "Artist1",
            imageLink = "image1",
            countryName = null,
            about = "About1",
            instagramLink = "instagram1",
            soundcloudLink = "soundcloud1",
            soundcloudFollowersCount = null
        )

        val artist2 = ArtistWriteDto(
            id = null,
            name = "Artist2",
            imageLink = "image2",
            countryName = null,
            about = "About2",
            instagramLink = "instagram2",
            soundcloudLink = "soundcloud2",
            soundcloudFollowersCount = null
        )

        val artist3 = ArtistWriteDto(
            id = null,
            name = "Artist3",
            imageLink = "image3",
            countryName = null,
            about = "About3",
            instagramLink = "instagram3",
            soundcloudLink = "soundcloud3",
            soundcloudFollowersCount = null
        )

        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist1), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist2), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist3), status().isCreated)

        val responseArtistsJson = makeGetRequest(mockMvc, artistEndpoint, status().isOk)
        val responseArtists = Json.decodeFromString<List<ArtistShortDto>>(responseArtistsJson)

        assertEquals(3, responseArtists.size)
    }

    @Test
    fun findOverallRating() {
        val artist1 = ArtistWriteDto(
            id = null,
            name = "Artist1",
            imageLink = "image1",
            countryName = "BE",
            about = "About1",
            instagramLink = "instagram1",
            soundcloudLink = "soundcloud1",
            soundcloudFollowersCount = 1000
        )

        val artist2 = ArtistWriteDto(
            id = null,
            name = "Artist2",
            imageLink = "image2",
            countryName = "BE",
            about = "About2",
            instagramLink = "instagram2",
            soundcloudLink = "soundcloud2",
            soundcloudFollowersCount = 100
        )

        val artist3 = ArtistWriteDto(
            id = null,
            name = "Artist3",
            imageLink = "image3",
            countryName = "BE",
            about = "About3",
            instagramLink = "instagram3",
            soundcloudLink = "soundcloud3",
            soundcloudFollowersCount = 10
        )

        val artist4 = ArtistWriteDto(
            id = null,
            name = "Artist4",
            imageLink = "image4",
            countryName = "BE",
            about = "About4",
            instagramLink = "instagram4",
            soundcloudLink = "soundcloud4",
            soundcloudFollowersCount = null
        )

        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist1), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist2), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist3), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist4), status().isCreated)

        val responseOverallRatingJson =
            makeGetRequest(mockMvc, "/artist/public/overallRating?cityName=BE&maxQuantity=10", status().isOk)
        val responseOverallRating = Json.decodeFromString<List<ArtistShortDto>>(responseOverallRatingJson)

        assertEquals(4, responseOverallRating.size)
        responseOverallRating.forEachIndexed { index, artistShortDto ->
            when (index) {
                0 -> {
                    assertEquals(artist1.name, artistShortDto.name)
                    assertEquals(200, artistShortDto.baseRating)
                    assertEquals(0, artistShortDto.overallFollowersCount)
                }
                1 -> {
                    assertEquals(artist2.name, artistShortDto.name)
                    assertEquals(20, artistShortDto.baseRating)
                    assertEquals(0, artistShortDto.overallFollowersCount)
                }
                2 -> {
                    assertEquals(artist3.name, artistShortDto.name)
                    assertEquals(2, artistShortDto.baseRating)
                    assertEquals(0, artistShortDto.overallFollowersCount)
                }
                3 -> {
                    assertEquals(artist4.name, artistShortDto.name)
                    assertEquals(0, artistShortDto.baseRating)
                    assertEquals(0, artistShortDto.overallFollowersCount)
                }
            }
        }
    }
}
