package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.service.ArtistService
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtistIntegrationTest (
    @Autowired private val artistService: ArtistService,
    @Autowired private val mockMvc: MockMvc,
) : AbstractPostgresTest() {

    @BeforeAll
    private fun createCountryForAssociations() {

        val country = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val countryJson = Json.encodeToString(country)
        val responseCountryJson = makePostRequest(mockMvc, "/country", countryJson, status().isCreated)
        val responseCountry = Json.decodeFromString<CountryDto>(responseCountryJson)
    }

    @AfterEach
    private fun cleanDb() = artistService.findAll().forEach { artistService.deleteById(it.id) }
//        countryService.findAll().forEach { countryService.deleteByName(it.name) }

    private fun saveOrUpdateArtist(
        artist: ArtistWriteDto, endpoint: String, expectedStatus: ResultMatcher,
        mockMvcFunction: (MockMvc, String, String, ResultMatcher) -> String
    ): ArtistFullDto {

        val artistJson = Json.encodeToString(artist)
        val responseArtistJson = mockMvcFunction(mockMvc, endpoint, artistJson, expectedStatus)
        val responseArtist = Json.decodeFromString<ArtistFullDto>(responseArtistJson)
        return responseArtist
    }

    val postFunc = { mockMvc: MockMvc, endpoint: String, body: String, expectedStatus: ResultMatcher ->
        makePostRequest(
            mockMvc,
            endpoint,
            body,
            expectedStatus
        )
    }

    val putFunc = { mockMvc: MockMvc, endpoint: String, body: String, expectedStatus: ResultMatcher ->
        makePutRequest(
            mockMvc,
            endpoint,
            body,
            expectedStatus
        )
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

        val responseArtist = saveOrUpdateArtist(
            artist = artistToSave,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

        assertNotNull(responseArtist.id)
        assertEquals("Amelie Lens", responseArtist.name)
        assertEquals(20, responseArtist.baseRating)
        assertEquals(0, responseArtist.overallFollowersCount)
        assertEquals("image", responseArtist.imageLink)
        assertEquals("soundcloud", responseArtist.soundcloudLink)
        assertEquals("instagram", responseArtist.instagramLink)
        assertEquals("About Amelie", responseArtist.about)
        assertEquals("BE", responseArtist.country?.name)
        assertEquals("+7", responseArtist.country?.phoneCode)
        assertEquals("EBE", responseArtist.country?.emojiCode)
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

        val responseSavedArtist = saveOrUpdateArtist(
            artist = artistToSave,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

        val artistToUpdate = artistToSave.copy(
            id = responseSavedArtist.id,
            name = "Amelie Lens2",
            imageLink = "image2",
            soundcloudLink = "soundcloud2",
            instagramLink = "instagram2",
            about = "About Amelie2",
            countryName = null,
            // this must not impact base rating
            soundcloudFollowersCount = 100000,
        )

        val responseUpdatedArtist = saveOrUpdateArtist(
            artist = artistToUpdate,
            endpoint = "/artist", expectedStatus = status().isOk,
            mockMvcFunction = putFunc
        )

        assertEquals(responseSavedArtist.id, responseUpdatedArtist.id)
        assertEquals("Amelie Lens2", responseUpdatedArtist.name)
        assertEquals(20, responseUpdatedArtist.baseRating)
        assertEquals(0, responseUpdatedArtist.overallFollowersCount)
        assertEquals("image2", responseUpdatedArtist.imageLink)
        assertEquals("soundcloud2", responseUpdatedArtist.soundcloudLink)
        assertEquals("instagram2", responseUpdatedArtist.instagramLink)
        assertEquals("About Amelie2", responseUpdatedArtist.about)
        assertNull(responseUpdatedArtist.country)
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

        val responseSavedArtist = saveOrUpdateArtist(
            artist = artistToSave,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

        val responseDeletedArtistJson = makeDeleteRequest(mockMvc, "/artist/${responseSavedArtist.id}", status().isOk)
        val responseDeletedArtist = Json.decodeFromString<ArtistFullDto>(responseDeletedArtistJson)

        assertEquals(responseSavedArtist.id, responseDeletedArtist.id)
        assertEquals(artistToSave.name, responseDeletedArtist.name)

        val responseFindArtistJson = makeGetRequest(mockMvc, "/artist", status().isOk)
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

        val responseSavedArtist1 = saveOrUpdateArtist(
            artist = artist1,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

        val responseSavedArtist2 = saveOrUpdateArtist(
            artist = artist2,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

        val responseSavedArtist3 = saveOrUpdateArtist(
            artist = artist3,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

        assertNotNull(responseSavedArtist1.id)
        assertEquals(artist1.name, responseSavedArtist1.name)
        assertNull(responseSavedArtist1.country)
        assertNotNull(responseSavedArtist2.id)
        assertEquals(artist2.name, responseSavedArtist2.name)
        assertNull(responseSavedArtist2.country)
        assertNotNull(responseSavedArtist3.id)
        assertEquals(artist3.name, responseSavedArtist3.name)
        assertNull(responseSavedArtist3.country)

        val responseArtistsJson = makeGetRequest(mockMvc, "/artist", status().isOk)
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

        val responseSavedArtist1 = saveOrUpdateArtist(
            artist = artist1,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

        val responseSavedArtist2 = saveOrUpdateArtist(
            artist = artist2,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

        val responseSavedArtist3 = saveOrUpdateArtist(
            artist = artist3,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

        val responseSavedArtist4 = saveOrUpdateArtist(
            artist = artist4,
            endpoint = "/artist", expectedStatus = status().isCreated,
            mockMvcFunction = postFunc
        )

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
