package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.*
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.EventService
import com.postraves.backend.postraveswiki.service.followable.MyUserProfileService
import com.postraves.backend.postraveswiki.service.followable.PlaceService
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import com.postraves.backend.postraveswiki.utils.Components.customRedisProvider
import com.postraves.backend.postraveswiki.utils.Endpoints.eventEndpoint
import com.postraves.backend.postraveswiki.utils.MockAuthentication
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.TestEntity.cityBrugesTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.placeBrugesTest
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
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import redis.embedded.RedisServer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventRatingIntegrationTest(
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
    @Autowired
    private val mockMvc: MockMvc,
    @Autowired
    private val eventService: EventService,
    @Autowired
    private val countryService: CountryService,
    @Autowired
    private val cityService: CityService,
    @Autowired
    private val placeService: PlaceService,
    @Autowired
    private val artistService: ArtistService,
    @Qualifier("eventCountryQuickRepoImpl")
    private val eventCountryQuickRepoImpl: EntityCountryQuickRepo,
    @Qualifier("eventOverallFollowersQuickRepoImpl")
    private val eventOverallFollowersQuickRepoImpl: FollowersQuickRepo,
    @Qualifier("eventWeeklyFollowersQuickRepoImpl")
    private val eventWeeklyFollowersQuickRepoImpl: FollowersQuickRepo,
) : AbstractPostgresTest() {

    @SpyBean
    private lateinit var dateTimeProvider: DateTimeProvider

    @SpyBean
    private lateinit var myUserProfileService: MyUserProfileService

    @MockBean
    private lateinit var securityService: SecurityService

    private val redisServer = RedisServer(customRedisProvider, redisPort)

    init {
        redisServer.start()
    }

    private val countryTestData2 = countryBeTest.copy(
        name = "RU",
        phoneCode = "+8",
    )

    private val cityTest2 = cityBrugesTest.copy(
        name = "Moscow",
        countryName = "RU",
        timeOffset = 0
    )

    private val placeTest2 = placeBrugesTest.copy(
        name = "Club2",
        cityName = "Moscow"
    )

    var persistedPlaceId: Long = 1
    var persistedPlace2Id: Long = 2

    private val eventTestData = EventWriteDto(
        id = null,
        name = "Event1",
        imageLink = "image1",
        about = "About Event1",
        ticketsLink = "link1",
        startDateTime = OffsetDateTime.of(2021, 8, 19, 0, 0, 0, 0, ZoneOffset.UTC),
        endDateTime = OffsetDateTime.of(2021, 8, 19, 6, 0, 0, 0, ZoneOffset.UTC),
        ticketPrices = emptyList(),
        // place id must be changed to real one of persisted place
        placeId = 1,
        organizers = emptySet(),
    )

    @BeforeAll
    private fun createCountryForAssociations() {
        SecurityContextHolder.getContext().authentication = MockAuthentication.authAdminTest

        makePostRequest(mockMvc, "/country", Json.encodeToString(countryBeTest), status().isCreated)
        makePostRequest(mockMvc, "/country", Json.encodeToString(countryTestData2), status().isCreated)
        makePostRequest(mockMvc, "/city", Json.encodeToString(cityBrugesTest), status().isCreated)
        makePostRequest(mockMvc, "/city", Json.encodeToString(cityTest2), status().isCreated)
        val persistedPlaceJson = makePostRequest(mockMvc, "/place", Json.encodeToString(placeBrugesTest), status().isCreated)
        val persistedPlace = Json.decodeFromString<PlaceShortDto>(persistedPlaceJson)
        persistedPlaceId = persistedPlace.id
        val persistedPlace2Json =
            makePostRequest(mockMvc, "/place", Json.encodeToString(placeTest2), status().isCreated)
        val persistedPlace2 = Json.decodeFromString<PlaceShortDto>(persistedPlace2Json)
        persistedPlace2Id = persistedPlace2.id
    }

    @AfterEach
    private fun cleanDb() {
        eventService.findAll().forEach { eventService.deleteById(it.id) }
        artistService.findAll().forEach { artistService.deleteById(it.id) }
    }

    @AfterAll
    private fun cleanUp() {
        eventService.findAll().forEach { eventService.deleteById(it.id) }
        artistService.findAll().forEach { artistService.deleteById(it.id) }
        placeService.findAll().forEach { placeService.deleteById(it.id) }
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    fun getRelevantEventsByRating() {
        val now = OffsetDateTime.now(UTC).withHour(0)

        Mockito.doReturn(userTest).`when`(securityService).user
        Mockito.doReturn("abc").`when`(securityService).firebaseAuthUid
        Mockito.doReturn(now).`when`(dateTimeProvider).getNow()

        val event1 = eventTestData.copy(placeId = persistedPlaceId)

        val event2 = event1.copy(
            name = "Event2",
            startDateTime = now.minusHours(1),
            endDateTime = now.plusHours(3)
        )

        val event3 = event1.copy(
            name = "Event3",
            startDateTime = now.plusHours(1),
            endDateTime = now.plusHours(3),
        )

        val event4 = event1.copy(
            name = "Event4",
            startDateTime = now.plusDays(1),
            endDateTime = now.plusDays(1).plusHours(2)
//            OffsetDateTime.parse("20-12-03T10:15:30+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )

        val event5 = event1.copy(
            name = "Event5",
            startDateTime = now.plusDays(1).plusHours(1),
            endDateTime = now.plusDays(1).plusHours(3)
        )

        val event6 = event1.copy(
            name = "Event6",
            startDateTime = now.plusDays(25),
            endDateTime = now.plusDays(25).plusHours(2)
        )

        val event7 = event1.copy(
            name = "Event7",
            ticketsLink = null,
            startDateTime = now.plusDays(25).plusHours(1),
            endDateTime = now.plusDays(25).plusHours(2)
        )

        val event8 = event1.copy(
            name = "Event8",
            ticketsLink = null,
            startDateTime = now.plusDays(25).plusHours(1),
            endDateTime = now.plusDays(25).plusHours(2),
            placeId = persistedPlace2Id
        )

        val event9 = event1.copy(
            name = "Event9",
            startDateTime = now.plusDays(100),
            endDateTime = now.plusDays(100).plusHours(2)
        )

        val event1Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        val event2Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event2), status().isCreated)
        val event3Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event3), status().isCreated)
        val event4Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event4), status().isCreated)
        val event5Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event5), status().isCreated)
        val event6Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event6), status().isCreated)
        val event7Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event7), status().isCreated)
        val event8Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event8), status().isCreated)
        val event9Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event9), status().isCreated)

        val event1Id = Json.decodeFromString<EventShortDto>(event1Json).id
        val event2Id = Json.decodeFromString<EventShortDto>(event2Json).id
        val event3Id = Json.decodeFromString<EventShortDto>(event3Json).id
        val event4Id = Json.decodeFromString<EventShortDto>(event4Json).id
        val event5Id = Json.decodeFromString<EventShortDto>(event5Json).id
        val event6Id = Json.decodeFromString<EventShortDto>(event6Json).id
        val event7Id = Json.decodeFromString<EventShortDto>(event7Json).id
        val event8Id = Json.decodeFromString<EventShortDto>(event8Json).id
        val event9Id = Json.decodeFromString<EventShortDto>(event9Json).id

        eventService.incrementFollowers(event1Id)

        var i = 0
        while (i < 5) {
            eventService.incrementFollowers(event2Id)
            i++
        }

        i = 0
        while (i < 10) {
            eventService.incrementFollowers(event3Id)
            i++
        }
        i = 0
        while (i < 15) {
            eventService.incrementFollowers(event4Id)
            i++
        }
        i = 0
        while (i < 20) {
            eventService.incrementFollowers(event5Id)
            i++
        }
        i = 0
        while (i < 4) {
            eventService.decrementFollowers(event5Id)
            i++
        }
        i = 0
        while (i < 25) {
            eventService.incrementFollowers(event6Id)
            i++
        }
        i = 0
        while (i < 30) {
            eventService.incrementFollowers(event7Id)
            i++
        }
        i = 0
        while (i < 35) {
            eventService.incrementFollowers(event8Id)
            i++
        }
        i = 0
        while (i < 40) {
            eventService.incrementFollowers(event9Id)
            i++
        }

        val cityName = placeBrugesTest.cityName

        val relevantEventsByRatingJson = makeGetRequest(
            mockMvc,
            "$eventEndpoint/public/relevantByRating?cityName=$cityName",
            status().isOk
        )
        val relevantEventsByRating = Json.decodeFromString<List<EventShortDto>>(relevantEventsByRatingJson)

        assertEquals(6, relevantEventsByRating.size)

        relevantEventsByRating.forEachIndexed { index, event ->
            when (index) {
                0 -> {
                    assertEquals(event7Id, event.id)
                    assertEquals(event7.name, event.name)
                    assertEquals(30, event.overallFollowers)
                    assertEquals(30, event.weeklyFollowers)
                }
                1 -> {
                    assertEquals(event6Id, event.id)
                    assertEquals(event6.name, event.name)
                    assertEquals(25, event.overallFollowers)
                    assertEquals(25, event.weeklyFollowers)
                }
                2 -> {
                    assertEquals(event5Id, event.id)
                    assertEquals(event5.name, event.name)
                    assertEquals(16, event.overallFollowers)
                    assertEquals(16, event.weeklyFollowers)
                }
                3 -> {
                    assertEquals(event4Id, event.id)
                    assertEquals(event4.name, event.name)
                    assertEquals(15, event.overallFollowers)
                    assertEquals(15, event.weeklyFollowers)
                }
                4 -> {
                    assertEquals(event3Id, event.id)
                    assertEquals(event3.name, event.name)
                    assertEquals(10, event.overallFollowers)
                    assertEquals(10, event.weeklyFollowers)
                }
                5 -> {
                    assertEquals(event2Id, event.id)
                    assertEquals(event2.name, event.name)
                    assertEquals(5, event.overallFollowers)
                    assertEquals(5, event.weeklyFollowers)
                }
            }
        }
    }
}
