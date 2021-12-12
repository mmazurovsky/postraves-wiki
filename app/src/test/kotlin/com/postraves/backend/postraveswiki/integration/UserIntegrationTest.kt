package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.exception.FollowingException
import com.postraves.backend.postraveswiki.repo.followable.MyUserProfileRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.*
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.MyUserProfileService
import com.postraves.backend.postraveswiki.utils.Components.customRedisProvider
import com.postraves.backend.postraveswiki.utils.MockAuthentication
import com.postraves.backend.postraveswiki.utils.MockAuthentication.createAuthByUser
import com.postraves.backend.postraveswiki.utils.TestEntity.artistBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.cityBrugesTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.userTest
import com.postraves.backend.postraveswiki.utils.TestEntity.userToSaveTest
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
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

    private val redisServer = RedisServer(customRedisProvider, redisPort)
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
        SecurityContextHolder.getContext().authentication = createAuthByUser(null)

        countryService.save(countryBeTest)
        cityService.save(cityBrugesTest)
    }

    @AfterEach
    private fun cleanDb() {
        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        myUserProfileService.deleteMyProfile()
        artistService.findAll().forEach { artistService.deleteById(it.id) }
    }

    @AfterAll
    private fun cleanUp() {
        artistService.findAll().forEach { artistService.deleteById(it.id) }
        cityService.findAll().forEach { cityService.deleteByName(it.name) }
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    fun getUserForAuthUidNotExistingInDb() {
        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        val result = securityService.user
        assertNull(result)
    }

    @Test
    fun saveUserAndFindIt() {

        `when`(securityService.firebaseAuthUid).thenReturn("abc")

        myUserProfileService.save(userToSaveTest)
        val savedUserFromMethodForSecurityService = myUserProfileService.getUserByAuthUidForSecurityService("abc")

        assertEquals(userToSaveTest.name, savedUserFromMethodForSecurityService?.name)
        assertEquals(userToSaveTest.currentCity, savedUserFromMethodForSecurityService?.currentCity?.name)
    }

    @Test
    fun followArtistAndGetFollows() {

        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        val savedUserId = myUserProfileService.save(userToSaveTest).id
        Mockito.doReturn(savedUserId).`when`(myUserProfileService).getMyUserId()

        val artistId = artistService.save(artistBeTest).id

        myUserProfileService.followArtist(artistId)

        val followed = myUserProfileService.findMyFollowingArtists()

        assertEquals(1, followed.size)
        assertEquals(artistId, followed[0].id)
        assertEquals(artistBeTest.name, followed[0].name)
        assertEquals(artistBeTest.imageLink, followed[0].imageLink)
        assertEquals(artistBeTest.countryName, followed[0].country!!.name)
        assertEquals(1, followed[0].overallFollowers)
        assertEquals(1, followed[0].weeklyFollowers)
    }

    @Test
    fun getIsFollowedAndFollowArtistAndAgainGetIsFollowed() {

        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        val savedUserId = myUserProfileService.save(userToSaveTest).id
        Mockito.doReturn(savedUserId).`when`(myUserProfileService).getMyUserId()
//        `when`(myUserProfileService.getMyUserId()).thenReturn(savedUserId)

        val artistId = artistService.save(artistBeTest).id

        val artistNotFollowed = artistService.findById(artistId)

        assertEquals(artistId, artistNotFollowed.id)
        assertEquals(artistBeTest.name, artistNotFollowed.name)
        assertEquals(artistBeTest.imageLink, artistNotFollowed.imageLink)
        assertEquals(artistBeTest.countryName, artistNotFollowed.country!!.name)
        assertEquals(false, artistNotFollowed.isFollowed)
        assertEquals(0, artistNotFollowed.overallFollowers)
        assertEquals(0, artistNotFollowed.weeklyFollowers)

        myUserProfileService.followArtist(artistId)

        val artistFollowed = artistService.findById(artistId)

        assertEquals(artistId, artistFollowed.id)
        assertEquals(artistBeTest.name, artistFollowed.name)
        assertEquals(artistBeTest.imageLink, artistFollowed.imageLink)
        assertEquals(artistBeTest.countryName, artistFollowed.country!!.name)
        assertEquals(true, artistFollowed.isFollowed)
        assertEquals(1, artistFollowed.overallFollowers)
        assertEquals(1, artistFollowed.weeklyFollowers)
    }

    @Test
    fun tryToFollowAndUnfollowSameArtistMultipleTimes() {

        `when`(securityService.firebaseAuthUid).thenReturn("abc")
        val savedUserId = myUserProfileService.save(userToSaveTest).id
        Mockito.doReturn(savedUserId).`when`(myUserProfileService).getMyUserId()

        val artistId = artistService.save(artistBeTest).id

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