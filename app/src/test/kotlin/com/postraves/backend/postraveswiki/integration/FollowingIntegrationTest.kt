package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
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
import com.postraves.backend.postraveswiki.utils.Requests.makeDeleteRequest
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePutRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.mockito.Mockito
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

    private val countryTestData = CountryWriteDto(
        name = "BE",
        nameRu = "NameRu",
        nameEn = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        phoneCode = "+7",

        )

    private val city = CityWriteDto(
        name = "Bruges",
        nameRu = "NameRu",
        nameEn = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        countryName = "BE",
        timeOffset = -3
    )

    private val artistTestData = ArtistWriteDto(
        id = null,
        name = "Amelie Lens",
        imageLink = "image",
        soundcloudUsername = "soundcloud",
        instagramUsername = "instagram",
        about = "About Amelie",
        countryName = countryTestData.name,
    )

    private val unityTestData = UnityWriteDto(
        id = null,
        name = "Unity 1",
        imageLink = "image 1",
        soundcloudUsername = "soundcloud 1",
        instagramUsername = "instagram 1",
        bandcampUsername = "bandcamp 1",
        about = "About 1",
        countryName = countryTestData.name,
    )

    private val userToSave = UserWriteDto(
        name = "Mika",
        imageLink = null,
        about = null,
        instagramUsername = null,
        telegramUsername = null,
        currentCity = "Bruges"
    )

    private var savedUserMimic = UserFullDto(
        id = 0,
        name = userToSave.name,
        currentCity = CityDto(
            timeOffset = 1,
            name = "Bruges",
            localName = "Bruges",
            country = CountryDto(
                name = "BE",
                localName = "Belgium",
                emojiCode = "",
                phoneCode = "",
            )
        ),
        about = null,
        imageLink = null,
        instagramUsername = null,
        telegramUsername = null,
    )

    @BeforeAll
    private fun prepare() {
        logger.info("Following Integration Test started")

        Mockito.doReturn(savedUserMimic).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        makePostRequest(mockMvc, "/country", Json.encodeToString(countryTestData), status().isCreated)
        makePostRequest(mockMvc, "/city", Json.encodeToString(city), status().isCreated)
        val userJson =
            makePostRequest(mockMvc, "/user/public/myProfile", Json.encodeToString(userToSave), status().isCreated)
        val userSaved = Json.decodeFromString<UserShortDto>(userJson)
        savedUserMimic = savedUserMimic.copy(id = userSaved.id)
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
        logger.info("Following Integration Test ended")
    }

    @Test
    @Order(1)
    fun saveArtistAndFollowAndGetItAndUnfollowAndGetIt() {
        Mockito.doReturn(savedUserMimic).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        val artistToSaveWithIdThatCanBeSameAsUsersIdIfThisArtistIsFirstToSaveInDb = artistTestData

        val artistIdRespJsonFirstId =
            makePostRequest(
                mockMvc,
                artistEndpoint,
                Json.encodeToString(artistToSaveWithIdThatCanBeSameAsUsersIdIfThisArtistIsFirstToSaveInDb),
                status().isCreated
            )
        val artistIdFirstId = Json.decodeFromString<ArtistShortDto>(artistIdRespJsonFirstId).id

        val artistToSave = artistTestData.copy(name = "AnotherArtist")

        val artistIdRespJson =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id


        val artistRespJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val savedArtist = Json.decodeFromString<ArtistFullDto>(artistRespJson)

        makePostRequest(mockMvc, "/user/myFollowing/artist/${savedArtist.id}", null, status().isOk)

        val updatedArtistJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val updatedArtist = Json.decodeFromString<ArtistFullDto>(updatedArtistJson)

        val artistsInOverallRating = artistOverallFollowersQuickRepoImpl.findTop(-1)
        val artistsInWeeklyRating = artistWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertNotNull(updatedArtist.id)
        assertEquals(artistToSave.name, updatedArtist.name)
        assertEquals(1, updatedArtist.overallFollowers)
        assertEquals(1, updatedArtist.weeklyFollowers)
        assertEquals(true, updatedArtist.isFollowed)

        assertEquals(1, artistsInOverallRating[updatedArtist.id])
        assertEquals(1, artistsInWeeklyRating[updatedArtist.id])

        makeDeleteRequest(mockMvc, "/user/myFollowing/artist/${savedArtist.id}", status().isOk)
        val updatedArtistJson2 = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val updatedArtist2 = Json.decodeFromString<ArtistFullDto>(updatedArtistJson2)

        assertEquals(updatedArtist.id, updatedArtist2.id)
        assertEquals(artistToSave.name, updatedArtist2.name)
        assertEquals(0, updatedArtist2.overallFollowers)
        assertEquals(0, updatedArtist2.weeklyFollowers)
        assertEquals(false, updatedArtist2.isFollowed)
    }

    @Test
    @Order(2)
    fun cleanupCheck() {
        Mockito.doReturn(savedUserMimic).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        val artists = artistService.findAll()
        val unities = unityService.findAll()
        val artistsInOverallRating = artistOverallFollowersQuickRepoImpl.findTop(-1)
        val artistsInWeeklyRating = artistWeeklyFollowersQuickRepoImpl.findTop(-1)
        val unitiesInOverallRating = unityOverallFollowersQuickRepoImpl.findTop(-1)
        val unitiesInWeeklyRating = unityWeeklyFollowersQuickRepoImpl.findTop(-1)
        val userFollows = userProfileService.findMyFollowingArtists()

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
        Mockito.doReturn(savedUserMimic).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        val unity1 = unityTestData

        val unitySavedJson = makePostRequest(mockMvc, unityEndpoint, Json.encodeToString(unity1), status().isCreated)
        val unitySavedId = Json.decodeFromString<UnityShortDto>(unitySavedJson).id

        val artist1 = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudUsername = "soundcloud",
            instagramUsername = "instagram",
            about = "About Amelie",
            countryName = countryTestData.name,
        )

        val artist2 = artist1.copy(
            name = "Artist2",
            imageLink = "image2",
            countryName = null,
            about = "About2",
            instagramUsername = "instagram2",
            soundcloudUsername = "soundcloud2",
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

        makePostRequest(mockMvc, "/user/myFollowing/artist/$savedArtist1Id", null, status().isOk)

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

    @Test
    @Order(4)
    fun saveArtistAndSearchForIt() {
        Mockito.doReturn(savedUserMimic).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        val artistToSave = artistTestData

        val artistIdRespJson =
            makePostRequest(mockMvc, artistEndpoint, Json.encodeToString(artistToSave), status().isCreated)
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id


        val artistRespJson = makeGetRequest(mockMvc, "$artistEndpoint/public/$artistId", status().isOk)
        val savedArtist = Json.decodeFromString<ArtistFullDto>(artistRespJson)

        makePostRequest(mockMvc, "/user/myFollowing/artist/${savedArtist.id}", null, status().isOk)

        val searchPhrase = artistToSave.name
        val searchResults = makeGetRequest(mockMvc, "$artistEndpoint/public/search/$searchPhrase", status().isOk)
        val searchResultsDecoded = Json.decodeFromString<List<ArtistShortDto>>(searchResults)

        assertEquals(1, searchResultsDecoded.size)
        assertEquals(artistToSave.name, searchResultsDecoded[0].name)
        assertEquals(artistId, searchResultsDecoded[0].id)
        assertEquals(1, searchResultsDecoded[0].overallFollowers)
        assertEquals(1, searchResultsDecoded[0].weeklyFollowers)
    }
}
