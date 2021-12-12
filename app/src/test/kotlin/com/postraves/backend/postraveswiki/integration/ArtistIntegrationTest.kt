package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.utils.Components.customRedisProvider
import com.postraves.backend.postraveswiki.utils.Endpoints.artistEndpoint
import com.postraves.backend.postraveswiki.utils.Endpoints.countryEndpoint
import com.postraves.backend.postraveswiki.utils.MockAuthentication.authAdminTest
import com.postraves.backend.postraveswiki.utils.Requests.makeDeleteRequest
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePutRequest
import com.postraves.backend.postraveswiki.utils.TestEntity.artistBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryBeTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import redis.embedded.RedisServer
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtistIntegrationTest(
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
    @Autowired
    private val mockMvc: MockMvc,
    @Autowired
    private val artistService: ArtistService,
    @Autowired
    private val countryService: CountryService,
    @Qualifier("artistCountryQuickRepoImpl")
    private val artistCountryQuickRepoImpl: EntityCountryQuickRepo,
    @Qualifier("artistOverallFollowersQuickRepoImpl")
    private val artistOverallFollowersQuickRepoImpl: FollowersQuickRepo,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val artistWeeklyFollowersQuickRepoImpl: FollowersQuickRepo,
) : AbstractPostgresTest() {

    private val redisServer = RedisServer(customRedisProvider, redisPort)
    init {
        redisServer.start()
    }

    @BeforeAll
    private fun createCountryForAssociations() {
        SecurityContextHolder.getContext().setAuthentication(authAdminTest)
        makePostRequest(mockMvc, countryEndpoint, Json.encodeToString(countryBeTest), status().isCreated)
    }

    @AfterEach
    private fun cleanDb() = artistService.findAll().forEach { artistService.deleteById(it.id) }

    @AfterAll
    private fun cleanUp() {
        artistService.findAll().forEach { artistService.deleteById(it.id) }
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    fun saveArtistWithCountryAssociation() {

        val artistToSave = artistBeTest

        val artistIdRespJson =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id

        val artistRespJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val savedArtist = Json.decodeFromString<ArtistFullDto>(artistRespJson)

        val countryArtistsInQuickRepo = artistCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val artistsInOverallRating = artistOverallFollowersQuickRepoImpl.findTop(-1)
        val artistsInWeeklyRating = artistWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertNotNull(savedArtist.id)
        assertEquals(artistToSave.name, savedArtist.name)
        assertEquals(0, savedArtist.overallFollowers)
        assertEquals(0, savedArtist.weeklyFollowers)
        assertEquals(artistToSave.imageLink, savedArtist.imageLink)
        assertEquals(artistToSave.soundcloudUsername, savedArtist.soundcloudUsername)
        assertEquals(artistToSave.instagramUsername, savedArtist.instagramUsername)
        assertEquals(artistToSave.about, savedArtist.about)
        assertEquals(artistToSave.countryName, savedArtist.country?.name)
        assertEquals(countryBeTest.phoneCode, savedArtist.country?.phoneCode)
        assertNotNull(savedArtist.country?.emojiCode)

        assert(countryArtistsInQuickRepo.contains(savedArtist.id))
        assert(artistsInOverallRating.contains(savedArtist.id))
        assert(artistsInWeeklyRating.contains(savedArtist.id))
    }

    @Test
    fun saveArtistWithSameNameMultipleTimes() {

        val artistToSave = artistBeTest
        val artistIdRespJson =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id

        val artistToSave2 = artistBeTest.copy(imageLink = "image2")
        val artistIdRespJson2 =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave2), status().isCreated)
        val artistId2 = Json.decodeFromString<ArtistShortDto>(artistIdRespJson2).id

        val artistRespJson1 = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val savedArtist1 = Json.decodeFromString<ArtistFullDto>(artistRespJson1)

        val artistRespJson2 = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId2", status().isOk)
        val savedArtist2 = Json.decodeFromString<ArtistFullDto>(artistRespJson2)

        assertNotNull(savedArtist1.id)
        assertNotNull(savedArtist2.id)
        assertEquals(savedArtist1.name, savedArtist2.name)
        assertNotEquals(savedArtist1, savedArtist2)
    }

    @Test
    fun updateArtistAndDeleteCountryAssociation() {

        val artistToSave = artistBeTest

        val responseSavedArtist =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val savedId = Json.decodeFromString<ArtistShortDto>(responseSavedArtist).id

        val artistToUpdate = artistToSave.copy(
            id = savedId,
            name = "Amelie Lens2",
            imageLink = "image2",
            soundcloudUsername = "soundcloud2",
            instagramUsername = "instagram2",
            about = "About Amelie2",
            countryName = null,
        )

        makePutRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToUpdate), status().isOk)

        val updatedJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$savedId", status().isOk)
        val updatedArtist = Json.decodeFromString<ArtistFullDto>(updatedJson)

        val countryArtistsInQuickRepo = artistCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val artistsInOverallRating = artistOverallFollowersQuickRepoImpl.findTop(-1)
        val artistsInWeeklyRating = artistWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(artistToUpdate.id, updatedArtist.id)
        assertEquals(artistToUpdate.name, updatedArtist.name)
        assertEquals(0, updatedArtist.overallFollowers)
        assertEquals(0, updatedArtist.weeklyFollowers)
        assertEquals(artistToUpdate.imageLink, updatedArtist.imageLink)
        assertEquals(artistToUpdate.soundcloudUsername, updatedArtist.soundcloudUsername)
        assertEquals(artistToUpdate.instagramUsername, updatedArtist.instagramUsername)
        assertEquals(artistToUpdate.about, updatedArtist.about)
        assertNull(updatedArtist.country)

        assert(!countryArtistsInQuickRepo.contains(updatedArtist.id))
        assert(artistsInOverallRating.contains(updatedArtist.id))
        assert(artistsInWeeklyRating.contains(updatedArtist.id))

    }

    @Test
    fun deleteArtistById() {

        val artistToSave = artistBeTest

        val responseSavedArtist =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val savedId = Json.decodeFromString<ArtistShortDto>(responseSavedArtist).id

        makeDeleteRequest(mockMvc, "$artistEndpoint/$savedId", status().isOk)

        val responseFindArtistJson = makeGetRequest(mockMvc, artistEndpoint, status().isOk)
        val responseFindArtist = Json.decodeFromString<List<ArtistShortDto>>(responseFindArtistJson)

        val countryArtistsInQuickRepo = artistCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val artistsInOverallRating = artistOverallFollowersQuickRepoImpl.findTop(-1)
        val artistsInWeeklyRating = artistWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(0, responseFindArtist.size)

        assert(!countryArtistsInQuickRepo.contains(savedId))
        assert(!artistsInOverallRating.contains(savedId))
        assert(!artistsInWeeklyRating.contains(savedId))
    }

    @Test
    fun saveMultipleArtistsAndFindAll() {

        val artist1 = artistBeTest

        val artist2 = artist1.copy(
            name = "Artist2",
            imageLink = "image2",
            countryName = null,
            about = "About2",
            instagramUsername = "instagram2",
            soundcloudUsername = "soundcloud2",
        )

        val artist3 = artist1.copy(
            name = "Artist3",
            imageLink = "image3",
            countryName = null,
            about = "About3",
            instagramUsername = "instagram3",
            soundcloudUsername = "soundcloud3",
        )

        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist1), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist2), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist3), status().isCreated)

        val responseArtistsJson = makeGetRequest(mockMvc, artistEndpoint, status().isOk)
        val responseArtists = Json.decodeFromString<List<ArtistShortDto>>(responseArtistsJson)

        val countryArtistsInQuickRepo = artistCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val artistsInOverallRating = artistOverallFollowersQuickRepoImpl.findTop(-1)
        val artistsInWeeklyRating = artistWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(3, responseArtists.size)
        responseArtists.forEach {
            assert(it.name == artist1.name || it.name == artist2.name || it.name == artist3.name)
            if (it.name == artist1.name) {
                assertEquals(artist1.imageLink, it.imageLink)
                assertEquals(artist1.countryName, it.country!!.name)
            } else if (it.name == artist2.name) {
                assertEquals(artist2.imageLink, it.imageLink)
                assertNull(it.country)
            }
        }
        // artist1 has country
        assertEquals(1, countryArtistsInQuickRepo.size)
        assertEquals(3, artistsInOverallRating.size)
        assertEquals(3, artistsInWeeklyRating.size)
    }

    @Test
    fun saveMultipleAndFindByName() {

        val artist1 = artistBeTest

        val artist2 = artist1.copy(
            name = "Artist2",
        )

        val artist3 = artist1.copy(
            name = "Artist3",
        )

        val artist4 = artist1.copy(
            name = "Tis",
        )

        val artist5 = artist1.copy(
            name = "tiS",
        )

        val artist6 = artist1.copy(
            name = "ti",
        )

        val artist7 = artist1.copy(
            name = "sit",
        )

        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist1), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist2), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist3), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist4), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist5), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist6), status().isCreated)
        makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist7), status().isCreated)

        val searchPhrase = "tis"
        val searchResults = makeGetRequest(mockMvc, "$artistEndpoint/public/search/$searchPhrase", status().isOk)
        val searchResultsDecoded = Json.decodeFromString<List<ArtistShortDto>>(searchResults)

        assertEquals(4, searchResultsDecoded.size)
        searchResultsDecoded.forEach {
            assert(
                it.name == artist2.name ||
                        it.name == artist3.name ||
                        it.name == artist4.name ||
                        it.name == artist5.name
            )
        }
    }
    // todo check update country to another one in redis repo
}
