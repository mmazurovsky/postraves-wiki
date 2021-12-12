package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.reading.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.data.enum.UserProfileRole
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.security.SecurityFilter
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.MoneyCurrencyService
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.utils.Components
import com.postraves.backend.postraveswiki.utils.Endpoints.artistEndpoint
import com.postraves.backend.postraveswiki.utils.Endpoints.cityEndpoint
import com.postraves.backend.postraveswiki.utils.Endpoints.countryEndpoint
import com.postraves.backend.postraveswiki.utils.MockAuthentication.authAdminTest
import com.postraves.backend.postraveswiki.utils.Requests
import com.postraves.backend.postraveswiki.utils.TestEntity.artistBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.cityBrugesTest
import com.postraves.backend.postraveswiki.utils.TestEntity.cityMoscowTest
import com.postraves.backend.postraveswiki.utils.TestEntity.cityTorontoTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryCaTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryRuTest
import com.postraves.backend.postraveswiki.utils.TestEntity.userTest
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import redis.embedded.RedisExecProvider
import redis.embedded.RedisServer
import redis.embedded.util.Architecture
import redis.embedded.util.OS
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ArtistRatingIntegrationTest(
    @Autowired
    private val artistService: ArtistService,
    @Autowired
    private val countryService: CountryService,
    @Autowired
    private val mockMvc: MockMvc,
    @Qualifier("artistCountryQuickRepoImpl")
    private val artistCountryQuickRepoImpl: EntityCountryQuickRepo,
    @Qualifier("artistOverallFollowersQuickRepoImpl")
    private val artistOverallFollowersQuickRepoImpl: FollowersQuickRepo,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val artistWeeklyFollowersQuickRepoImpl: FollowersQuickRepo,
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
) : AbstractPostgresTest() {

    @MockBean
    private lateinit var securityService: SecurityService

    private val redisServer = RedisServer(Components.customRedisProvider, redisPort)
    init {
        redisServer.start()
    }
    
    @BeforeAll
    private fun createCountryAndCityForAssociations() {

        SecurityContextHolder.getContext().authentication = authAdminTest
        
        val countryJson1 = Json.encodeToString(countryBeTest)
        val countryJson2 = Json.encodeToString(countryRuTest)
        val countryJson3 = Json.encodeToString(countryCaTest)
        val cityJson1 = Json.encodeToString(cityBrugesTest)
        val cityJson2 = Json.encodeToString(cityMoscowTest)
        val cityJson3 = Json.encodeToString(cityTorontoTest)

        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson1, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson2, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson3, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson1, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson2, MockMvcResultMatchers.status().isCreated)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson3, MockMvcResultMatchers.status().isCreated)

        artistService.removeBestOfTheWeekByCityInCountry(cityBrugesTest.name)
        artistService.removeBestOfTheWeekByCityInCountry(cityMoscowTest.name)
        artistService.removeBestOfTheWeekByCityInCountry(cityTorontoTest.name)
    }

    @AfterEach
    private fun cleanDb() = artistService.findAll().forEach { artistService.deleteById(it.id) }

    @AfterAll
    private fun cleanUp() {
        artistService.removeBestOfTheWeekByCityInCountry(cityBrugesTest.name)
        artistService.removeBestOfTheWeekByCityInCountry(cityMoscowTest.name)
        artistService.removeBestOfTheWeekByCityInCountry(cityTorontoTest.name)

        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    @Order(1)
    fun saveArtistsAndIncrementFollowersAndFindOverallRating() {
        Mockito.doReturn(userTest).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        val artist1 = artistBeTest
        val artist2 = artist1.copy(name = "Artist2")
        val artist3 = artist1.copy(name = "Artist3")
        val artist4 = artist1.copy(name = "Artist4")

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
    @Order(2)
    fun saveArtistsAndIncrementFollowersAndFindWeeklyRating() {
        Mockito.doReturn(userTest).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        val artist1 = artistBeTest
        val artist2 = artist1.copy(name = "Artist2")
        val artist3 = artist1.copy(name = "Artist3")
        val artist4 = artist1.copy(name = "Artist4")

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
    @Order(3)
    fun saveArtistsAndIncrementFollowersAndChangeCountriesOfArtistsAndGetOverallRating() {
        Mockito.doReturn(userTest).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        val artist1 = artistBeTest
        val artist2 = artist1.copy(name = "Artist2")
        val artist3 = artist1.copy(name = "Artist3")
        val artist4 = artist1.copy(name = "Artist4")

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
    @Order(4)
    fun saveArtistsAndIncrementFollowersAndChangeCountriesOfArtistsAndGetWeeklyRating() {
        Mockito.doReturn(userTest).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        val artist1 = artistBeTest
        val artist2 = artist1.copy(name = "Artist2")
        val artist3 = artist1.copy(name = "Artist3")
        val artist4 = artist1.copy(name = "Artist4")

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
    @Order(5)
    fun getBestOfTheWeekWhenItIsNotSet() {
        val artistOfTheWeek = artistService.findBestOfTheWeekByCityInCountry("Bruges")
        val artistsInCountryRedisBE = artistCountryQuickRepoImpl.getAllIdsByCountry("BE")
        val artistsInCountryRedisRU = artistCountryQuickRepoImpl.getAllIdsByCountry("RU")
        val artistsInCountryRedisCA = artistCountryQuickRepoImpl.getAllIdsByCountry("CA")
        val artistsOverallTopRedis = artistOverallFollowersQuickRepoImpl.findTop(-1)
        val artistsWeeklyTopRedis = artistWeeklyFollowersQuickRepoImpl.findTop(-1)
        assertEquals(0, artistsInCountryRedisBE.size)
        assertEquals(0, artistsInCountryRedisRU.size)
        assertEquals(0, artistsInCountryRedisCA.size)
        assertEquals(0, artistsOverallTopRedis.size)
        assertEquals(0, artistsWeeklyTopRedis.size)
        assertNull(artistOfTheWeek)
    }

    @Test
    @Order(6)
    fun saveArtistsAndSetWeeklyBestAndGetWeeklyBestAndSetAgainAndGetAgain() {
        Mockito.doReturn(userTest).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid

        val artist1 = artistBeTest
        val artist2 = artist1.copy(name = "Artist2")
        val artist3 = artist1.copy(name = "Artist3")
        val artist4 = artist1.copy(name = "Artist4")

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

        artistService.incrementFollowers(artist2Id)
        artistService.incrementFollowers(artist2Id)

        artistService.setBestOfTheWeekForAllCities()
        Thread.sleep(2_000)
        val artistOfTheWeekNew = artistService.findBestOfTheWeekByCityInCountry("Bruges")

        assertEquals(artist2Id, artistOfTheWeekNew!!.id)
        assertEquals(artist2.name, artistOfTheWeekNew!!.name)
        assertEquals(artist2.countryName, artistOfTheWeekNew!!.country!!.name)
    }
}
