package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.UnityService
import com.postraves.backend.postraveswiki.utils.Components.customRedisProvider
import com.postraves.backend.postraveswiki.utils.Endpoints.artistEndpoint
import com.postraves.backend.postraveswiki.utils.Endpoints.unityEndpoint
import com.postraves.backend.postraveswiki.utils.MockAuthentication
import com.postraves.backend.postraveswiki.utils.Requests.makeDeleteRequest
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePutRequest
import com.postraves.backend.postraveswiki.utils.TestEntity.artistBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.unityBeTest
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnityIntegrationTest(
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
    @Autowired
    private val mockMvc: MockMvc,
    @Autowired
    private val unityService: UnityService,
    @Autowired
    private val artistService: ArtistService,
    @Autowired
    private val countryService: CountryService,
    @Qualifier("unityCountryQuickRepoImpl")
    private val unityCountryQuickRepoImpl: EntityCountryQuickRepo,
    @Qualifier("unityOverallFollowersQuickRepoImpl")
    private val unityOverallFollowersQuickRepoImpl: FollowersQuickRepo,
    @Qualifier("unityWeeklyFollowersQuickRepoImpl")
    private val unityWeeklyFollowersQuickRepoImpl: FollowersQuickRepo,
) : AbstractPostgresTest() {

    private val redisServer = RedisServer(customRedisProvider, redisPort)
    init {
        redisServer.start()
    }

    @BeforeAll
    private fun createCountryForAssociations() {
        SecurityContextHolder.getContext().authentication = MockAuthentication.authAdminTest

        makePostRequest(mockMvc, "/country", Json.encodeToString(countryBeTest), status().isCreated)
    }

    @AfterEach
    private fun cleanDb() {
        unityService.findAll().forEach { unityService.deleteById(it.id) }
        artistService.findAll().forEach { artistService.deleteById(it.id) }
    }

    @AfterAll
    private fun cleanUp() {
        unityService.findAll().forEach { unityService.deleteById(it.id) }
        artistService.findAll().forEach { artistService.deleteById(it.id) }
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    fun saveUnityWithCountryAssociation() {

        val unityToSave = unityBeTest

        val unityIdRespJson =
            makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unityToSave), status().isCreated)
        val unityId = Json.decodeFromString<UnityShortDto>(unityIdRespJson).id

        val unityRespJson = makeGetRequest(mockMvc, "$unityEndpoint/public/$unityId", status().isOk)
        val savedUnity = Json.decodeFromString<UnityFullDto>(unityRespJson)

        val countryUnitysInQuickRepo = unityCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val unitysInOverallRating = unityOverallFollowersQuickRepoImpl.findTop(-1)
        val unitysInWeeklyRating = unityWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertNotNull(savedUnity.id)
        assertEquals(unityToSave.name, savedUnity.name)
        assertEquals(0, savedUnity.overallFollowers)
        assertEquals(0, savedUnity.weeklyFollowers)
        assertEquals(unityToSave.imageLink, savedUnity.imageLink)
        assertEquals(unityToSave.soundcloudUsername, savedUnity.soundcloudUsername)
        assertEquals(unityToSave.instagramUsername, savedUnity.instagramUsername)
        assertEquals(unityToSave.bandcampUsername, savedUnity.bandcampUsername)
        assertEquals(unityToSave.about, savedUnity.about)
        assertEquals(unityToSave.countryName, savedUnity.country?.name)
        assertEquals(countryBeTest.phoneCode, savedUnity.country?.phoneCode)
        assertNotNull(savedUnity.country?.emojiCode)

        assert(countryUnitysInQuickRepo.contains(savedUnity.id))
        assert(unitysInOverallRating.contains(savedUnity.id))
        assert(unitysInWeeklyRating.contains(savedUnity.id))
    }

    @Test
    fun updateUnityAndDeleteCountryAssociation() {

        val unityToSave = unityBeTest

        val responseSavedUnity =
            makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unityToSave), status().isCreated)
        val savedId = Json.decodeFromString<UnityShortDto>(responseSavedUnity).id

        val unityToUpdate = unityToSave.copy(
            id = savedId,
            name = "Unity 2",
            imageLink = "image 2",
            soundcloudUsername = "soundcloud 2",
            instagramUsername = "instagram 2",
            bandcampUsername = "bandcamp 2",
            about = "About 2",
            countryName = null,
        )

        makePutRequest(mockMvc, unityEndpoint, Json.encodeToString(unityToUpdate), status().isOk)

        val updatedJson = makeGetRequest(mockMvc, "$unityEndpoint/public/$savedId", status().isOk)
        val updatedUnity = Json.decodeFromString<UnityFullDto>(updatedJson)

        val countryUnitysInQuickRepo = unityCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val unitysInOverallRating = unityOverallFollowersQuickRepoImpl.findTop(-1)
        val unitysInWeeklyRating = unityWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(unityToUpdate.id, updatedUnity.id)
        assertEquals(unityToUpdate.name, updatedUnity.name)
        assertEquals(0, updatedUnity.overallFollowers)
        assertEquals(0, updatedUnity.weeklyFollowers)
        assertEquals(unityToUpdate.imageLink, updatedUnity.imageLink)
        assertEquals(unityToUpdate.soundcloudUsername, updatedUnity.soundcloudUsername)
        assertEquals(unityToUpdate.instagramUsername, updatedUnity.instagramUsername)
        assertEquals(unityToUpdate.bandcampUsername, updatedUnity.bandcampUsername)
        assertEquals(unityToUpdate.about, updatedUnity.about)
        assertNull(updatedUnity.country)

        assert(!countryUnitysInQuickRepo.contains(updatedUnity.id))
        assert(unitysInOverallRating.contains(updatedUnity.id))
        assert(unitysInWeeklyRating.contains(updatedUnity.id))
    }

    @Test
    fun deleteUnityById() {

        val unityToSave = unityBeTest

        val responseSavedUnity =
            makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unityToSave), status().isCreated)
        val savedId = Json.decodeFromString<UnityShortDto>(responseSavedUnity).id

        makeDeleteRequest(mockMvc, "$unityEndpoint/$savedId", status().isOk)

        val responseFindUnityJson = makeGetRequest(mockMvc, unityEndpoint, status().isOk)
        val responseFindUnity = Json.decodeFromString<List<UnityShortDto>>(responseFindUnityJson)

        val countryUnitysInQuickRepo = unityCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val unitysInOverallRating = unityOverallFollowersQuickRepoImpl.findTop(-1)
        val unitysInWeeklyRating = unityWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(0, responseFindUnity.size)

        assert(!countryUnitysInQuickRepo.contains(savedId))
        assert(!unitysInOverallRating.contains(savedId))
        assert(!unitysInWeeklyRating.contains(savedId))
    }

    @Test
    fun saveMultipleUnitysAndFindAll() {
        val unity1 = unityBeTest

        val unity2 = unity1.copy(
            name = "Unity2",
            imageLink = "image2",
            countryName = null,
            about = "About2",
            instagramUsername = "instagram2",
            soundcloudUsername = "soundcloud2",
        )

        val unity3 = unity1.copy(
            name = "Unity3",
            imageLink = "image3",
            countryName = null,
            about = "About3",
            instagramUsername = "instagram3",
            soundcloudUsername = "soundcloud3",
        )

        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity1), status().isCreated)
        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity2), status().isCreated)
        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity3), status().isCreated)

        val responseUnitysJson = makeGetRequest(mockMvc, unityEndpoint, status().isOk)
        val responseUnitys = Json.decodeFromString<List<UnityShortDto>>(responseUnitysJson)

        val countryUnitysInQuickRepo = unityCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val unitysInOverallRating = unityOverallFollowersQuickRepoImpl.findTop(-1)
        val unitysInWeeklyRating = unityWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(3, responseUnitys.size)
        responseUnitys.forEach {
            assert(it.name == unity1.name || it.name == unity2.name || it.name == unity3.name)
            if (it.name == unity1.name) {
                assertEquals(unity1.imageLink, it.imageLink)
                assertEquals(unity1.countryName, it.country!!.name)
            } else if (it.name == unity2.name) {
                assertEquals(unity2.imageLink, it.imageLink)
                assertNull(it.country)
            }
        }
        // unity1 has country
        assertEquals(1, countryUnitysInQuickRepo.size)
        assertEquals(3, unitysInOverallRating.size)
        assertEquals(3, unitysInWeeklyRating.size)
    }

    @Test
    fun saveMultipleAndFindByName() {
        val unity1 = unityBeTest

        val unity2 = unity1.copy(
            name = "Unity2",
        )

        val unity3 = unity1.copy(
            name = "Unity3tisEND",
        )

        val unity4 = unity1.copy(
            name = "Tis",
        )

        val unity5 = unity1.copy(
            name = "tiS",
        )

        val unity6 = unity1.copy(
            name = "ti",
        )

        val unity7 = unity1.copy(
            name = "sit",
        )

        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity1), status().isCreated)
        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity2), status().isCreated)
        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity3), status().isCreated)
        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity4), status().isCreated)
        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity5), status().isCreated)
        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity6), status().isCreated)
        makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity7), status().isCreated)

        val searchPhrase = "tis"
        val searchResults = makeGetRequest(mockMvc, "$unityEndpoint/public/search/$searchPhrase", status().isOk)
        val searchResultsDecoded = Json.decodeFromString<List<UnityShortDto>>(searchResults)

        assertEquals(3, searchResultsDecoded.size)
        searchResultsDecoded.forEach {
            assert(it.name == unity2.name ||
                    it.name == unity3.name ||
                    it.name == unity4.name ||
                    it.name == unity5.name)
        }
    }

    // todo check update country to another one in redis repo
    
    @Test
    fun getArtistsOfUnityAndAddThemAndGetThemAgain() {

        val unity1 = unityBeTest

        val unitySavedJson = makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity1), status().isCreated)
        val unitySavedId = Json.decodeFromString<UnityShortDto>(unitySavedJson).id

        val artistsOfUnityJson = makeGetRequest(mockMvc, "$unityEndpoint/public/$unitySavedId/artists", status().isOk)
        val artistsOfUnity = Json.decodeFromString<List<ArtistShortDto>>(artistsOfUnityJson)

        assertTrue(artistsOfUnity.isEmpty())

        val artist2 = artistBeTest.copy(
            name = "Artist2",
            imageLink = "image2",
            countryName = null,
            about = "About2",
            instagramUsername = "instagram2",
            soundcloudUsername = "soundcloud2",
        )

        val artist3 = artistBeTest.copy(
            name = "Artist3",
            imageLink = "image3",
            countryName = null,
            about = "About3",
            instagramUsername = "instagram3",
            soundcloudUsername = "soundcloud3",
        )

        val savedArtist1Json = makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistBeTest), status().isCreated)
        val savedArtist2Json = makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist2), status().isCreated)
        val savedArtist3Json = makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist3), status().isCreated)

        val savedArtist1Id = Json.decodeFromString<ArtistShortDto>(savedArtist1Json).id
        val savedArtist2Id = Json.decodeFromString<ArtistShortDto>(savedArtist2Json).id
        val savedArtist3Id = Json.decodeFromString<ArtistShortDto>(savedArtist3Json).id

        makePutRequest(mockMvc, "$unityEndpoint/$unitySavedId/artists", Json.encodeToString(setOf(savedArtist1Id, savedArtist2Id, savedArtist3Id)), status().isOk)
    
        val artistsOfUnityUpdatedJson = makeGetRequest(mockMvc, "$unityEndpoint/public/$unitySavedId/artists", status().isOk)
        val artistsOfUnityUpdated = Json.decodeFromString<List<ArtistShortDto>>(artistsOfUnityUpdatedJson)
        
        assertEquals(3, artistsOfUnityUpdated.size)
        artistsOfUnityUpdated.forEach {
            assert(setOf(savedArtist1Id, savedArtist2Id, savedArtist3Id).contains(it.id))
            when (it.id) {
                savedArtist1Id -> {
                    assertEquals(artistBeTest.name, it.name)
                    assertEquals(artistBeTest.imageLink, it.imageLink)
                    assertEquals(artistBeTest.countryName, it.country!!.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                savedArtist2Id -> {
                    assertEquals(artist2.name, it.name)
                    assertEquals(artist2.imageLink, it.imageLink)
                    assertNull(it.country)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                savedArtist3Id -> {
                    assertEquals(artist3.name, it.name)
                    assertEquals(artist3.imageLink, it.imageLink)
                    assertNull(it.country)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
            }
        }
    }

    @Test
    fun addArtistsToUnityAndUpdateArtistsOfUnityAndGetThem() {

        val unity1 = unityBeTest

        val unitySavedJson = makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity1), status().isCreated)
        val unitySavedId = Json.decodeFromString<UnityShortDto>(unitySavedJson).id

        val artist1 = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudUsername = "soundcloud",
            instagramUsername = "instagram",
            about = "About Amelie",
            countryName = countryBeTest.name,
        )

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

        val artist4 = artist1.copy(
            name = "Artist4",
            imageLink = "image4",
            countryName = null,
            about = "About4",
            instagramUsername = "instagram4",
            soundcloudUsername = "soundcloud4",
        )

        val savedArtist1Json =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist1), status().isCreated)
        val savedArtist2Json =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist2), status().isCreated)
        val savedArtist3Json =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist3), status().isCreated)
        val savedArtist4Json =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist4), status().isCreated)

        val savedArtist1Id = Json.decodeFromString<ArtistShortDto>(savedArtist1Json).id
        val savedArtist2Id = Json.decodeFromString<ArtistShortDto>(savedArtist2Json).id
        val savedArtist3Id = Json.decodeFromString<ArtistShortDto>(savedArtist3Json).id
        val savedArtist4Id = Json.decodeFromString<ArtistShortDto>(savedArtist4Json).id

        makePutRequest(
            mockMvc,
            "$unityEndpoint/$unitySavedId/artists",
            Json.encodeToString(setOf(savedArtist1Id, savedArtist2Id, savedArtist3Id)),
            status().isOk
        )

        makePutRequest(
            mockMvc,
            "$unityEndpoint/$unitySavedId/artists",
            Json.encodeToString(setOf(savedArtist1Id, savedArtist4Id)),
            status().isOk
        )

        val artistsOfUnityJson = makeGetRequest(mockMvc, "$unityEndpoint/public/$unitySavedId/artists", status().isOk)

        val artistsOfUnity = Json.decodeFromString<List<ArtistShortDto>>(artistsOfUnityJson)

        assertEquals(2, artistsOfUnity.size)
        artistsOfUnity.forEach {
            assert(setOf(savedArtist1Id, savedArtist4Id).contains(it.id))
            when (it.id) {
                savedArtist1Id -> {
                    assertEquals(artist1.name, it.name)
                    assertEquals(artist1.imageLink, it.imageLink)
                    assertEquals(artist1.countryName, it.country!!.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                savedArtist4Id -> {
                    assertEquals(artist4.name, it.name)
                    assertEquals(artist4.imageLink, it.imageLink)
                    assertNull(it.country)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
            }
        }
    }
}
