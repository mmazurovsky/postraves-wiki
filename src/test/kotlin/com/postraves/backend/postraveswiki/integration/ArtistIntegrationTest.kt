package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.service.ArtistService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.utils.Requests.makeDeleteRequest
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePutRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import redis.embedded.RedisServer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtistIntegrationTest(
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
        )

        val artistIdRespJson =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id

        val artistRespJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val artistDecoded = Json.decodeFromString<ArtistFullDto>(artistRespJson)

        assertNotNull(artistDecoded.id)
        assertEquals("Amelie Lens", artistDecoded.name)
        assertEquals(0, artistDecoded.overallFollowers)
        assertEquals(0, artistDecoded.weeklyFollowers)
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
        )

        makePutRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToUpdate), status().isOk)

        val updatedJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$savedId", status().isOk)
        val updatedArtist = Json.decodeFromString<ArtistFullDto>(updatedJson)

        assertEquals(savedId, updatedArtist.id)
        assertEquals("Amelie Lens2", updatedArtist.name)
        assertEquals(0, updatedArtist.overallFollowers)
        assertEquals(0, updatedArtist.weeklyFollowers)
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
        )

        val responseSavedArtist =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
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
        )

        val artist2 = ArtistWriteDto(
            id = null,
            name = "Artist2",
            imageLink = "image2",
            countryName = null,
            about = "About2",
            instagramLink = "instagram2",
            soundcloudLink = "soundcloud2",
        )

        val artist3 = ArtistWriteDto(
            id = null,
            name = "Artist3",
            imageLink = "image3",
            countryName = null,
            about = "About3",
            instagramLink = "instagram3",
            soundcloudLink = "soundcloud3",
        )

        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist1), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist2), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist3), status().isCreated)

        val responseArtistsJson = makeGetRequest(mockMvc, artistEndpoint, status().isOk)
        val responseArtists = Json.decodeFromString<List<ArtistShortDto>>(responseArtistsJson)

        assertEquals(3, responseArtists.size)
    }
}
