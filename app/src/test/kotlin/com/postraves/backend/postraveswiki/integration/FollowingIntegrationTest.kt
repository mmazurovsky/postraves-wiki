package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.UnityWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.MyUserProfileService
import com.postraves.backend.postraveswiki.service.followable.UnityService
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePutRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
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
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FollowingIntegrationTest(
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
    @Autowired
    private val mockMvc: MockMvc,
    @Autowired
    private val artistService: ArtistService,
    @Autowired
    private val unityService: UnityService,
    @Autowired
    private val countryService: CountryService,
    @Autowired
    private val cityService: CityService,
    @Autowired
    private val userProfileService: MyUserProfileService,
    @Qualifier("artistCountryQuickRepoImpl")
    private val artistCountryQuickRepoImpl: EntityCountryQuickRepo,
    @Qualifier("artistOverallFollowersQuickRepoImpl")
    private val artistOverallFollowersQuickRepoImpl: FollowersQuickRepo,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val artistWeeklyFollowersQuickRepoImpl: FollowersQuickRepo,
    @Qualifier("unityCountryQuickRepoImpl")
    private val unityCountryQuickRepoImpl: EntityCountryQuickRepo,
    @Qualifier("unityOverallFollowersQuickRepoImpl")
    private val unityOverallFollowersQuickRepoImpl: FollowersQuickRepo,
    @Qualifier("unityWeeklyFollowersQuickRepoImpl")
    private val unityWeeklyFollowersQuickRepoImpl: FollowersQuickRepo,
) : AbstractPostgresTest() {

    @SpyBean
    private lateinit var myUserProfileService: MyUserProfileService
    @MockBean
    private lateinit var securityService: SecurityService

    private val artistEndpoint: String = "/artist"
    private val unityEndpoint: String = "/unity"

    private val redisServer: RedisServer = RedisServer(redisPort)

    init {
        redisServer.start()
    }

    private val countryTestData = CountryDto(
        name = "BE",
        phoneCode = "+7",
        emojiCode = "EBE"
    )

    private val city = CityWriteDto(
        name = "Bruges",
        countryName = "BE",
        timeOffset = -3
    )

    private val artistTestData = ArtistWriteDto(
        id = null,
        name = "Amelie Lens",
        imageLink = "image",
        soundcloudLink = "soundcloud",
        instagramLink = "instagram",
        about = "About Amelie",
        countryName = countryTestData.name,
    )

    private val unityTestData = UnityWriteDto(
        id = null,
        name = "Unity 1",
        imageLink = "image 1",
        soundcloudLink = "soundcloud 1",
        instagramLink = "instagram 1",
        bandcampLink = "bandcamp 1",
        about = "About 1",
        countryName = countryTestData.name,
    )

    private val userToSave = UserWriteDto(
        name = "Mika",
        imageLink = null,
        about = null,
        instagramLink = null,
        telegramLink = null,
        currentCity = "Bruges"
    )

    @BeforeAll
    private fun prepare() {
        `when`(securityService.userAuthUid).thenReturn("abc")
        doReturn("abc").`when`(myUserProfileService).getMyAuthUidOnlyIfUserProfileExists()

        makePostRequest(mockMvc, "/country", Json.encodeToString(countryTestData), status().isCreated)
        makePostRequest(mockMvc, "/city", Json.encodeToString(city), status().isCreated)
        makePostRequest(mockMvc, "/user/public/myProfile", Json.encodeToString(userToSave), status().isCreated)
    }

    @AfterEach
    private fun cleanDb() {
        artistService.findAll().forEach { artistService.deleteById(it.id) }
        unityService.findAll().forEach { unityService.deleteById(it.id) }
    }

    @AfterAll
    private fun cleanUp() {
        userProfileService.deleteMyProfile()
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        cityService.findAll().forEach { cityService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    @Order(1)
    fun saveArtistAndFollowAndGetIt() {
        `when`(securityService.userAuthUid).thenReturn("abc")
        doReturn("abc").`when`(myUserProfileService).getMyAuthUidOnlyIfUserProfileExists()

        val artistToSave = artistTestData

        val artistIdRespJson =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id

        val artistRespJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val savedArtist = Json.decodeFromString<ArtistFullDto>(artistRespJson)

        makePostRequest(mockMvc, "/user/myFollows/artist/${savedArtist.id}", null, status().isOk)

        val updatedArtistJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val updatedArtist = Json.decodeFromString<ArtistFullDto>(updatedArtistJson)

        val artistsInOverallRating = artistOverallFollowersQuickRepoImpl.findTop(-1)
        val artistsInWeeklyRating = artistWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertNotNull(updatedArtist.id)
        assertEquals(artistToSave.name, updatedArtist.name)
        assertEquals(1, updatedArtist.overallFollowers)
        assertEquals(1, updatedArtist.weeklyFollowers)

        assertEquals(1, artistsInOverallRating[updatedArtist.id])
        assertEquals(1, artistsInWeeklyRating[updatedArtist.id])
    }

    @Test
    @Order(2)
    fun cleanupCheck() {
        `when`(securityService.userAuthUid).thenReturn("abc")
        doReturn("abc").`when`(myUserProfileService).getMyAuthUidOnlyIfUserProfileExists()

        val artists = artistService.findAll()
        val unities = unityService.findAll()
        val artistsInOverallRating = artistOverallFollowersQuickRepoImpl.findTop(-1)
        val artistsInWeeklyRating = artistWeeklyFollowersQuickRepoImpl.findTop(-1)
        val unitiesInOverallRating = unityOverallFollowersQuickRepoImpl.findTop(-1)
        val unitiesInWeeklyRating = unityWeeklyFollowersQuickRepoImpl.findTop(-1)
        val userFollows = userProfileService.findMyFollowsArtist()

        assertEquals(0, artists.size)
        assertEquals(0, unities.size)
        assertEquals(0, artistsInOverallRating.size)
        assertEquals(0, artistsInWeeklyRating.size)
        assertEquals(0, unitiesInOverallRating.size)
        assertEquals(0, unitiesInWeeklyRating.size)
        assertEquals(0, userFollows.size)
    }

    @Test
    @Order(3)
    fun addArtistsToUnityAndFollowOneArtistAndGetAllArtistsOfUnity() {
        `when`(securityService.userAuthUid).thenReturn("abc")
        doReturn("abc").`when`(myUserProfileService).getMyAuthUidOnlyIfUserProfileExists()

        val unity1 = unityTestData

        val unitySavedJson = makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity1), status().isCreated)
        val unitySavedId = Json.decodeFromString<UnityShortDto>(unitySavedJson).id

        val artist1 = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudLink = "soundcloud",
            instagramLink = "instagram",
            about = "About Amelie",
            countryName = countryTestData.name,
        )

        val artist2 = artist1.copy(
            name = "Artist2",
            imageLink = "image2",
            countryName = null,
            about = "About2",
            instagramLink = "instagram2",
            soundcloudLink = "soundcloud2",
        )

        val savedArtist1Json =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist1), status().isCreated)
        val savedArtist2Json =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artist2), status().isCreated)

        val savedArtist1Id = Json.decodeFromString<ArtistShortDto>(savedArtist1Json).id
        val savedArtist2Id = Json.decodeFromString<ArtistShortDto>(savedArtist2Json).id

        makePutRequest(
            mockMvc,
            "$unityEndpoint/$unitySavedId/artists",
            Json.encodeToString(setOf(savedArtist1Id, savedArtist2Id)),
            status().isOk
        )

        makePostRequest(mockMvc, "/user/myFollows/artist/$savedArtist1Id", null, status().isOk)

        val savedArtist1UpdatedJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$savedArtist1Id", status().isOk)
        val savedArtist1Updated = Json.decodeFromString<ArtistFullDto>(savedArtist1UpdatedJson)

        assertEquals(artist1.name, savedArtist1Updated.name)
        assertEquals(artist1.imageLink, savedArtist1Updated.imageLink)
        assertEquals(artist1.countryName, savedArtist1Updated.country!!.name)
        assertEquals(1, savedArtist1Updated.overallFollowers)
        assertEquals(1, savedArtist1Updated.weeklyFollowers)

        val artistsOfUnityJson = makeGetRequest(mockMvc, "$unityEndpoint/public/$unitySavedId/artists", status().isOk)

        val artistsOfUnity = Json.decodeFromString<List<ArtistShortDto>>(artistsOfUnityJson)

        assertEquals(2, artistsOfUnity.size)
        artistsOfUnity.forEach {
            assert(setOf(savedArtist1Id, savedArtist2Id).contains(it.id))
            when (it.id) {
                savedArtist1Id -> {
                    assertEquals(artist1.name, it.name)
                    assertEquals(artist1.imageLink, it.imageLink)
                    assertEquals(artist1.countryName, it.country!!.name)
                    assertEquals(1, it.overallFollowers)
                    assertEquals(1, it.weeklyFollowers)
                }
                savedArtist2Id -> {
                    assertEquals(artist2.name, it.name)
                    assertEquals(artist2.imageLink, it.imageLink)
                    assertNull(it.country)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
            }
        }
    }
}
