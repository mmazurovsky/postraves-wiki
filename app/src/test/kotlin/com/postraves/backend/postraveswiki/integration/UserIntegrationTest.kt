package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.exception.FollowingException
import com.postraves.backend.postraveswiki.repo.followable.MyUserProfileRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.*
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.MyUserProfileService
import com.postraves.backend.postraveswiki.utils.Requests
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
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
class UserIntegrationTest(
    @Autowired
    private val mockMvc: MockMvc,
    @Autowired
    private val artistService: ArtistService,
    @Autowired
    private val cityService: CityService,
    @Autowired
    private val countryService: CountryService,
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
) : AbstractPostgresTest() {

    private val redisServer = RedisServer(redisPort)

    init {
        redisServer.start()
    }

    @SpyBean
    private lateinit var myUserProfileService: MyUserProfileService

    @SpyBean
    private lateinit var myUserProfileRepo: MyUserProfileRepo

    @SpyBean
    private lateinit var securityService: SecurityService

    @BeforeAll
    private fun createCountryAndCityForAssociations() {

        logger.info("User Integration Test started")

        val country1 = CountryWriteDto(
            name = "BE",
            nameRu = "NameRu",
            nameEn = "NameUk",
            nameDe = "NameDe",
            nameFr = "NameFr",
            phoneCode = "+7",

            )
        val countryJson1 = Json.encodeToString(country1)
        Requests.makePostRequest(mockMvc, "/country", countryJson1, MockMvcResultMatchers.status().isCreated)

        val city = CityWriteDto(
            name = "Bruges",
            nameRu = "NameRu",
            nameEn = "NameUk",
            nameDe = "NameDe",
            nameFr = "NameFr",
            countryName = "BE",
            timeOffset = -3
        )
        val cityJson = Json.encodeToString(city)
        Requests.makePostRequest(mockMvc, "/city", cityJson, MockMvcResultMatchers.status().isCreated)
    }

    @AfterEach
    private fun cleanDb() {
        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        myUserProfileService.deleteMyProfile()
        artistService.findAll().forEach { artistService.deleteById(it.id) }
    }

    @AfterAll
    private fun cleanUp() {
        cityService.findAll().forEach { cityService.deleteByName(it.name) }
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
        logger.info("User Integration Test ended")
    }

    @Test
    fun getUserForAuthUidNotExistingInDb() {
        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        val result = securityService.user
        assertNull(result)
    }

    @Test
    fun saveUserAndFindIt() {
        val userToSave = UserWriteDto(
            name = "Mika",
            imageLink = null,
            about = null,
            instagramUsername = null,
            telegramUsername = null,
            currentCity = "Bruges"
        )

        `when`(securityService.firebaseAuthUid).thenReturn("abc")

        myUserProfileService.save(userToSave)
        val savedUserFromMethodForSecurityService = myUserProfileService.findByAuthUidForSecurityService("abc")

        assertEquals(userToSave.name, savedUserFromMethodForSecurityService?.name)
        assertEquals(userToSave.currentCity, savedUserFromMethodForSecurityService?.currentCity?.name)
    }

    @Test
    fun followArtistAndGetFollows() {
        val userToSave = UserWriteDto(
            name = "Mika",
            imageLink = null,
            about = null,
            instagramUsername = null,
            telegramUsername = null,
            currentCity = "Bruges"
        )

        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        val savedUserId = myUserProfileService.save(userToSave).id
        Mockito.doReturn(savedUserId).`when`(myUserProfileService).getMyUserId()
//        `when`(myUserProfileService.getMyUserId()).thenReturn(savedUserId)

        val artistToSave = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudUsername = "soundcloud",
            instagramUsername = "instagram",
            about = "About Amelie",
            countryName = "BE",
        )
        val artistIdRespJson = Requests.makePostRequest(
            mockMvc,
            "/artist",
            Json.encodeToString(artistToSave),
            MockMvcResultMatchers.status().isCreated
        )
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id

        myUserProfileService.followArtist(artistId)

        val followed = myUserProfileService.findMyFollowingArtists()

        assertEquals(1, followed.size)
        assertEquals(artistId, followed[0].id)
        assertEquals(artistToSave.name, followed[0].name)
        assertEquals(artistToSave.imageLink, followed[0].imageLink)
        assertEquals(artistToSave.countryName, followed[0].country!!.name)
        assertEquals(1, followed[0].overallFollowers)
        assertEquals(1, followed[0].weeklyFollowers)
    }

    @Test
    fun getIsFollowedAndFollowArtistAndAgainGetIsFollowed() {
        val userToSave = UserWriteDto(
            name = "Mika",
            imageLink = null,
            about = null,
            instagramUsername = null,
            telegramUsername = null,
            currentCity = "Bruges"
        )

        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        val savedUserId = myUserProfileService.save(userToSave).id
        Mockito.doReturn(savedUserId).`when`(myUserProfileService).getMyUserId()
//        `when`(myUserProfileService.getMyUserId()).thenReturn(savedUserId)

        val artistToSave = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudUsername = "soundcloud",
            instagramUsername = "instagram",
            about = "About Amelie",
            countryName = "BE",
        )
        val artistIdRespJson = Requests.makePostRequest(
            mockMvc,
            "/artist",
            Json.encodeToString(artistToSave),
            MockMvcResultMatchers.status().isCreated
        )
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id

        val artistNotFollowed = artistService.findById(artistId)

        assertEquals(artistId, artistNotFollowed.id)
        assertEquals(artistToSave.name, artistNotFollowed.name)
        assertEquals(artistToSave.imageLink, artistNotFollowed.imageLink)
        assertEquals(artistToSave.countryName, artistNotFollowed.country!!.name)
        assertEquals(false, artistNotFollowed.isFollowed)
        assertEquals(0, artistNotFollowed.overallFollowers)
        assertEquals(0, artistNotFollowed.weeklyFollowers)

        myUserProfileService.followArtist(artistId)

        val artistFollowed = artistService.findById(artistId)

        assertEquals(artistId, artistFollowed.id)
        assertEquals(artistToSave.name, artistFollowed.name)
        assertEquals(artistToSave.imageLink, artistFollowed.imageLink)
        assertEquals(artistToSave.countryName, artistFollowed.country!!.name)
        assertEquals(true, artistFollowed.isFollowed)
        assertEquals(1, artistFollowed.overallFollowers)
        assertEquals(1, artistFollowed.weeklyFollowers)
    }

    @Test
    fun tryToFollowAndUnfollowSameArtistMultipleTimes() {
        val userToSave = UserWriteDto(
            name = "Mika",
            imageLink = null,
            about = null,
            instagramUsername = null,
            telegramUsername = null,
            currentCity = "Bruges"
        )

        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        val savedUserId = myUserProfileService.save(userToSave).id
        Mockito.doReturn(savedUserId).`when`(myUserProfileService).getMyUserId()

        val artistToSave = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudUsername = "soundcloud",
            instagramUsername = "instagram",
            about = "About Amelie",
            countryName = "BE",
        )
        val artistIdRespJson = Requests.makePostRequest(
            mockMvc,
            "/artist",
            Json.encodeToString(artistToSave),
            MockMvcResultMatchers.status().isCreated
        )
        val artistId = Json.decodeFromString<ArtistShortDto>(artistIdRespJson).id

        val isFollowed1 = myUserProfileRepo.checkArtistIsFollowed(savedUserId, artistId)
        myUserProfileService.followArtist(artistId)
        val isFollowed2 = myUserProfileRepo.checkArtistIsFollowed(savedUserId, artistId)
        myUserProfileService.unfollowArtist(artistId)
        val isFollowed3 = myUserProfileRepo.checkArtistIsFollowed(savedUserId, artistId)
        myUserProfileService.followArtist(artistId)
        val isFollowed4 = myUserProfileRepo.checkArtistIsFollowed(savedUserId, artistId)
        assertThrows<FollowingException> { myUserProfileService.followArtist(artistId) }
        val isFollowed5 = myUserProfileRepo.checkArtistIsFollowed(savedUserId, artistId)
        myUserProfileService.unfollowArtist(artistId)
        val isFollowed6 = myUserProfileRepo.checkArtistIsFollowed(savedUserId, artistId)
        assertThrows<FollowingException> { myUserProfileService.unfollowArtist(artistId) }

        assertEquals(false, isFollowed1)
        assertEquals(true, isFollowed2)
        assertEquals(false, isFollowed3)
        assertEquals(true, isFollowed4)
        assertEquals(true, isFollowed5)
        assertEquals(false, isFollowed6)
    }

}