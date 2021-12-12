package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.*
import com.postraves.backend.postraveswiki.data.enum.EventStatus
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.EventService
import com.postraves.backend.postraveswiki.service.followable.PlaceService
import com.postraves.backend.postraveswiki.service.followable.UnityService
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import com.postraves.backend.postraveswiki.utils.Components.customRedisProvider
import com.postraves.backend.postraveswiki.utils.Endpoints
import com.postraves.backend.postraveswiki.utils.Endpoints.eventEndpoint
import com.postraves.backend.postraveswiki.utils.MockAuthentication
import com.postraves.backend.postraveswiki.utils.Requests.makeDeleteRequest
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePutRequest
import com.postraves.backend.postraveswiki.utils.TestEntity.cityBrugesTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.currencyEurTest
import com.postraves.backend.postraveswiki.utils.TestEntity.currencyRubTest
import com.postraves.backend.postraveswiki.utils.TestEntity.currencyUsdTest
import com.postraves.backend.postraveswiki.utils.TestEntity.eventTest
import com.postraves.backend.postraveswiki.utils.TestEntity.placeBrugesTest
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
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import redis.embedded.RedisServer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.*

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventIntegrationTest(
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
    @Autowired
    private val mockMvc: MockMvc,
    @Autowired
    private val eventService: EventService,
    @Autowired
    private val countryService: CountryService,
    @Autowired
    private val unityService: UnityService,
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

    private val redisServer = RedisServer(customRedisProvider, redisPort)

    init {
        redisServer.start()
    }

    private val countryBeTest2 = countryBeTest.copy(
        name = "RU",
        phoneCode = "+8",
    )

    private val cityTest2 = cityBrugesTest.copy(
        name = "Moscow",
        countryName = "RU",
        timeOffset = 0
    )

    private val place1 = placeBrugesTest

    private val place2 = placeBrugesTest.copy(
        name = "Club2",
        cityName = "Moscow"
    )

    var persistedPlace1Id: Long = 1
    var persistedPlace2Id: Long = 2
    
    @BeforeAll
    private fun createCountryForAssociations() {
        SecurityContextHolder.getContext().setAuthentication(MockAuthentication.authAdminTest)

        listOf(
            currencyRubTest,
            currencyUsdTest,
            currencyEurTest,
        ).forEach {
            makePostRequest(mockMvc, "/moneyCurrency", Json.encodeToString(it), status().isCreated)
        }
        makePostRequest(mockMvc, "/country", Json.encodeToString(countryBeTest), status().isCreated)
        makePostRequest(mockMvc, "/country", Json.encodeToString(countryBeTest2), status().isCreated)
        makePostRequest(mockMvc, "/city", Json.encodeToString(cityBrugesTest), status().isCreated)
        makePostRequest(mockMvc, "/city", Json.encodeToString(cityTest2), status().isCreated)
        val persistedPlaceJson = makePostRequest(mockMvc, "/place", Json.encodeToString(place1), status().isCreated)
        val persistedPlace = Json.decodeFromString<PlaceShortDto>(persistedPlaceJson)
        persistedPlace1Id = persistedPlace.id
        val persistedPlace2Json =
            makePostRequest(mockMvc, "/place", Json.encodeToString(place2), status().isCreated)
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
        unityService.findAll().forEach { unityService.deleteById(it.id) }
        placeService.findAll().forEach { placeService.deleteById(it.id) }
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    fun saveEvent() {

        val eventToSave = eventTest.copy(placeId = persistedPlace1Id)

        val eventIdRespJson =
            makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(eventToSave), status().isCreated)
        val eventId = Json.decodeFromString<EventShortDto>(eventIdRespJson).id

        val eventRespJson = makeGetRequest(mockMvc, "$eventEndpoint/public/$eventId", status().isOk)
        val savedEvent = Json.decodeFromString<EventFullDto>(eventRespJson)

        val countryEventsInQuickRepo = eventCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)

        assertNotNull(savedEvent.id)
        assertEquals(eventToSave.name, savedEvent.name)
        assertEquals(0, savedEvent.overallFollowers)
        assertEquals(0, savedEvent.weeklyFollowers)
        assertEquals(eventToSave.imageLink, savedEvent.imageLink)
        assertEquals(eventToSave.about, savedEvent.about)
        assertEquals(eventToSave.ticketsLink, savedEvent.ticketsLink)
        assertEquals(eventToSave.ticketPrices, savedEvent.ticketPrices)
        assertEquals(eventToSave.startDateTime, savedEvent.startDateTime)
        assertEquals(eventToSave.endDateTime, savedEvent.endDateTime)
        assertEquals(EventStatus.PAST, savedEvent.status)
        assertEquals(place1.name, savedEvent.place.name)
        assertEquals(place1.imageLink, savedEvent.place.imageLink)
        assertEquals(place1.cityName, savedEvent.place.city.name)
        assertEquals(countryBeTest.name, savedEvent.place.city.country.name)

        assert(countryEventsInQuickRepo.contains(savedEvent.id))
    }

    @Test
    fun saveEventWithSameNameMultipleTimes() {

        val eventToSave = eventTest.copy(placeId = persistedPlace1Id)
        val eventIdRespJson =
            makePostRequest(mockMvc, Endpoints.eventEndpoint, Json.encodeToString(eventToSave), status().isCreated)
        val eventId = Json.decodeFromString<EventShortDto>(eventIdRespJson).id

        val eventToSave2 = eventToSave.copy(imageLink = "image2")
        val eventIdRespJson2 =
            makePostRequest(mockMvc, Endpoints.eventEndpoint, Json.encodeToString(eventToSave2), status().isCreated)
        val eventId2 = Json.decodeFromString<EventShortDto>(eventIdRespJson2).id

        val eventRespJson1 = makeGetRequest(mockMvc, "${Endpoints.eventEndpoint}/public/$eventId", status().isOk)
        val savedEvent1 = Json.decodeFromString<EventFullDto>(eventRespJson1)

        val eventRespJson2 = makeGetRequest(mockMvc, "${Endpoints.eventEndpoint}/public/$eventId2", status().isOk)
        val savedEvent2 = Json.decodeFromString<EventFullDto>(eventRespJson2)

        assertNotNull(savedEvent1.id)
        assertNotNull(savedEvent2.id)
        assertEquals(savedEvent1.name, savedEvent2.name)
        assertNotEquals(savedEvent1, savedEvent2)
    }

    @Test
    fun updateEventAndItsPlaceAndTicketPrices() {

        val eventToSave = eventTest.copy(placeId = persistedPlace1Id)

        val responseSavedEvent =
            makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(eventToSave), status().isCreated)
        val savedId = Json.decodeFromString<EventShortDto>(responseSavedEvent).id

        val eventToUpdate = eventToSave.copy(
            id = savedId,
            name = "Event2",
            imageLink = "image2",
            ticketsLink = null,
            about = "About Amelie2",
            placeId = persistedPlace2Id,
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "Ticket1",
                    price = 200.5,
                    currency = currencyRubTest.name
                ),
                TicketPriceWriteDto(
                    name = "Ticket2",
                    price = 300.0,
                    currency = currencyRubTest.name
                )
            )
        )

        makePutRequest(mockMvc, eventEndpoint, Json.encodeToString(eventToUpdate), status().isOk)

        val updatedJson = makeGetRequest(mockMvc, "$eventEndpoint/public/$savedId", status().isOk)
        val updatedEvent = Json.decodeFromString<EventFullDto>(updatedJson)

        val country1EventsInQuickRepo = eventCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val country2EventsInQuickRepo = eventCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest2.name)

        assertEquals(eventToUpdate.id, updatedEvent.id)
        assertEquals(eventToUpdate.name, updatedEvent.name)
        assertEquals(0, updatedEvent.overallFollowers)
        assertEquals(0, updatedEvent.weeklyFollowers)
        assertEquals(eventToUpdate.imageLink, updatedEvent.imageLink)
        assertEquals(eventToUpdate.about, updatedEvent.about)
        assertEquals(eventToUpdate.ticketsLink, updatedEvent.ticketsLink)
        eventToUpdate.ticketPrices!!.forEachIndexed { index, ticketPriceWriteDto ->
            assertEquals(eventToUpdate.ticketPrices!![index].name, updatedEvent.ticketPrices[index].name)
            assertEquals(eventToUpdate.ticketPrices!![index].price, updatedEvent.ticketPrices[index].price)
            assertEquals(eventToUpdate.ticketPrices!![index].currency, updatedEvent.ticketPrices[index].currency.name)
        }
        assertEquals(eventToUpdate.startDateTime, updatedEvent.startDateTime)
        assertEquals(eventToUpdate.endDateTime, updatedEvent.endDateTime)
        assertEquals(EventStatus.PAST, updatedEvent.status)
        assertEquals(place2.name, updatedEvent.place.name)
        assertEquals(place2.imageLink, updatedEvent.place.imageLink)
        assertEquals(place2.cityName, updatedEvent.place.city.name)
        assertEquals(countryBeTest2.name, updatedEvent.place.city.country.name)

        assert(!country1EventsInQuickRepo.contains(updatedEvent.id))
        assert(country2EventsInQuickRepo.contains(updatedEvent.id))
    }

    @Test
    fun deleteEventById() {

        val eventToSave = eventTest.copy(placeId = persistedPlace1Id)

        val responseSavedEvent =
            makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(eventToSave), status().isCreated)
        val savedId = Json.decodeFromString<EventShortDto>(responseSavedEvent).id

        makeDeleteRequest(mockMvc, "$eventEndpoint/$savedId", status().isOk)

        val responseFindEventJson = makeGetRequest(mockMvc, eventEndpoint, status().isOk)
        val responseFindEvent = Json.decodeFromString<List<EventShortDto>>(responseFindEventJson)

        val countryEventsInQuickRepo = eventCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val eventsInOverallRating = eventOverallFollowersQuickRepoImpl.findTop(-1)
        val eventsInWeeklyRating = eventWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(0, responseFindEvent.size)

        assert(!countryEventsInQuickRepo.contains(savedId))
        assert(!eventsInOverallRating.contains(savedId))
        assert(!eventsInWeeklyRating.contains(savedId))
    }

    @Test
    fun saveMultipleEventsAndFindAll() {
        val event1 = eventTest.copy(placeId = persistedPlace1Id)

        val event2 = event1.copy(
            name = "Event2",
            imageLink = "image2",
            about = "About2",
            startDateTime = OffsetDateTime.of(2021, 8, 20, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
            endDateTime = OffsetDateTime.of(2021, 8, 20, 6, 0, 0, 0, ZoneOffset.ofHours(0)),
            placeId = persistedPlace2Id
        )

        val event3 = event1.copy(
            name = "Event3",
            imageLink = "image3",
            about = "About3",
            startDateTime = OffsetDateTime.of(2021, 8, 21, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
            endDateTime = OffsetDateTime.of(2021, 8, 21, 6, 0, 0, 0, ZoneOffset.ofHours(0)),
            placeId = persistedPlace2Id
        )

        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event2), status().isCreated)
        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event3), status().isCreated)

        val responseEventsJson = makeGetRequest(mockMvc, eventEndpoint, status().isOk)
        val responseEvents = Json.decodeFromString<List<EventShortDto>>(responseEventsJson)

        val country1EventsInQuickRepo = eventCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val country2EventsInQuickRepo = eventCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest2.name)
        val eventsInOverallRating = eventOverallFollowersQuickRepoImpl.findTop(-1)
        val eventsInWeeklyRating = eventWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(3, responseEvents.size)
        responseEvents.forEach {
            assert(it.name == event1.name || it.name == event2.name || it.name == event3.name)
            when (it.name) {
                event1.name -> {
                    assertEquals(event1.imageLink, it.imageLink)
                    assertEquals(event1.placeId, it.place.id)
                }
                event2.name -> {
                    assertEquals(event2.imageLink, it.imageLink)
                    assertEquals(event2.placeId, it.place.id)
                }
                event3.name -> {
                    assertEquals(event3.imageLink, it.imageLink)
                    assertEquals(event3.placeId, it.place.id)
                }
            }
        }
        // event1 has country
        assertEquals(1, country1EventsInQuickRepo.size)
        assertEquals(2, country2EventsInQuickRepo.size)
        assertEquals(3, eventsInOverallRating.size)
        assertEquals(3, eventsInWeeklyRating.size)
    }

    @Test
    fun saveMultipleAndFindByName() {
        val event1 = eventTest.copy(placeId = persistedPlace1Id)

        val event2 = event1.copy(
            name = "Event2",
        )

        val event3 = event1.copy(
            name = "Event3",
        )

        val event4 = event1.copy(
            name = "Tis",
        )

        val event5 = event1.copy(
            name = "tiS",
        )

        val event6 = event1.copy(
            name = "ti",
        )

        val event7 = event1.copy(
            name = "sit",
        )

        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event2), status().isCreated)
        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event3), status().isCreated)
        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event4), status().isCreated)
        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event5), status().isCreated)
        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event6), status().isCreated)
        makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event7), status().isCreated)

        val searchPhrase = "tis"
        val searchResults = makeGetRequest(mockMvc, "$eventEndpoint/public/search/$searchPhrase", status().isOk)
        val searchResultsDecoded = Json.decodeFromString<List<EventShortDto>>(searchResults)

        assertEquals(2, searchResultsDecoded.size)
        searchResultsDecoded.forEach {
            assert(
                it.name == event4.name ||
                        it.name == event5.name
            )
        }
    }

    @Test
    fun getOrganizersAndUpdateOrganizersAndGetAgain() {
        val event1 = eventTest.copy(placeId = persistedPlace1Id)

        val eventJson = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        val eventSavedId = Json.decodeFromString<EventShortDto>(eventJson).id

        val orgsJson = makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/organizers", status().isOk)
        val orgs = Json.decodeFromString<List<UnityShortDto>>(orgsJson)

        assertTrue(orgs.isEmpty())

        val unity1 = UnityWriteDto(
            id = null,
            name = "Unity 1",
            imageLink = "image 1",
            soundcloudUsername = "soundcloud 1",
            instagramUsername = "instagram 1",
            bandcampUsername = "bandcamp 1",
            about = "About 1",
            countryName = countryBeTest.name,
        )

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

        val savedUnity1Json =
            makePostRequest(mockMvc, "/unity", Json.encodeToString(unity1), status().isCreated)
        val savedUnity2Json =
            makePostRequest(mockMvc, "/unity", Json.encodeToString(unity2), status().isCreated)
        val savedUnity3Json =
            makePostRequest(mockMvc, "/unity", Json.encodeToString(unity3), status().isCreated)

        val savedUnity1Id = Json.decodeFromString<UnityShortDto>(savedUnity1Json).id
        val savedUnity2Id = Json.decodeFromString<UnityShortDto>(savedUnity2Json).id
        val savedUnity3Id = Json.decodeFromString<UnityShortDto>(savedUnity3Json).id

        makePutRequest(
            mockMvc,
            "$eventEndpoint/$eventSavedId/organizers",
            Json.encodeToString(setOf(savedUnity1Id, savedUnity2Id, savedUnity3Id)),
            status().isOk
        )

        val orgsUpdatedJson = makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/organizers", status().isOk)
        val orgsUpdated = Json.decodeFromString<List<UnityShortDto>>(orgsUpdatedJson)

        assertEquals(3, orgsUpdated.size)
        orgsUpdated.forEach {
            when (it.id) {
                savedUnity1Id -> {
                    assertEquals(unity1.name, it.name)
                    assertEquals(unity1.imageLink, it.imageLink)
                    assertEquals(unity1.countryName, it.country!!.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                savedUnity2Id -> {
                    assertEquals(unity2.name, it.name)
                    assertEquals(unity2.imageLink, it.imageLink)
                    assertNull(it.country)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                savedUnity3Id -> {
                    assertEquals(unity3.name, it.name)
                    assertEquals(unity3.imageLink, it.imageLink)
                    assertNull(it.country)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
            }
        }
    }

    @Test
    fun getLineupAndAddTimetablePerformancesAndGetLineupAgain() {
        val event1 = eventTest.copy(placeId = persistedPlace1Id)

        val eventJson = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        val eventSavedId = Json.decodeFromString<EventShortDto>(eventJson).id

        val lineupJson = makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/lineup", status().isOk)
        val lineup = Json.decodeFromString<List<ArtistShortDto>>(lineupJson)

        assertTrue(lineup.isEmpty())

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
            name = "Artist2"
        )
        val artist3 = artist1.copy(
            name = "Artist3"
        )
        val artist4 = artist1.copy(
            name = "Artist4"
        )

        val artist1Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist1), status().isCreated)
        val artist2Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist2), status().isCreated)
        val artist3Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist3), status().isCreated)
        val artist4Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist4), status().isCreated)

        val artist1Id = Json.decodeFromString<ArtistShortDto>(artist1Json).id
        val artist2Id = Json.decodeFromString<ArtistShortDto>(artist2Json).id
        val artist3Id = Json.decodeFromString<ArtistShortDto>(artist3Json).id
        val artist4Id = Json.decodeFromString<ArtistShortDto>(artist4Json).id

        val timetablePerformance1 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id, artist2Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        val timetablePerformance2 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist3Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        val timetablePerformance3 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist4Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        makePutRequest(
            mockMvc,
            "$eventEndpoint/$eventSavedId/timetable",
            Json.encodeToString(listOf(timetablePerformance1, timetablePerformance2, timetablePerformance3)),
            status().isOk
        )

        val lineupUpdatedJson = makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/lineup", status().isOk)
        val lineupUpdated = Json.decodeFromString<List<ArtistShortDto>>(lineupUpdatedJson)

        assertEquals(4, lineupUpdated.size)
        val setOfIds = mutableSetOf<Long>()
        lineupUpdated.forEach {
            setOfIds.add(it.id)
            when (it.id) {
                artist1Id -> {
                    assertEquals(artist1.name, it.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                artist2Id -> {
                    assertEquals(artist2.name, it.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                artist3Id -> {
                    assertEquals(artist3.name, it.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                artist4Id -> {
                    assertEquals(artist4.name, it.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                else -> {
                    throw RuntimeException()
                }
            }
        }
        assertEquals(4, setOfIds.size)
    }

    @Test
    fun getLineupWhenThereIsOneSameArtistInTimetable() {
        val event1 = eventTest.copy(placeId = persistedPlace1Id)

        val scene1 = SceneDto(
            id = null,
            name = "Scene3",
            imageLink = "sceneImage3",
            priority = 3,
        )

        makePutRequest(
            mockMvc,
            "/place/$persistedPlace1Id/scenes",
            Json.encodeToString(listOf(scene1)),
            status().isOk
        )

        val savedScenesJson = makeGetRequest(mockMvc, "/place/public/$persistedPlace1Id/scenes", status().isOk)
        val savedScenes = Json.decodeFromString<List<SceneDto>>(savedScenesJson)
        val scene1Id = savedScenes.find { it.name == scene1.name }!!.id

        val eventJson = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        val eventSavedId = Json.decodeFromString<EventShortDto>(eventJson).id

        val lineupJson = makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/lineup", status().isOk)
        val lineup = Json.decodeFromString<List<ArtistShortDto>>(lineupJson)

        assertTrue(lineup.isEmpty())

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
            name = "Artist2"
        )
        val artist3 = artist1.copy(
            name = "Artist3"
        )
        val artist4 = artist1.copy(
            name = "Artist4"
        )

        val artist1Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist1), status().isCreated)
        val artist2Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist2), status().isCreated)
        val artist3Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist3), status().isCreated)
        val artist4Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist4), status().isCreated)

        val artist1Id = Json.decodeFromString<ArtistShortDto>(artist1Json).id
        val artist2Id = Json.decodeFromString<ArtistShortDto>(artist2Json).id
        val artist3Id = Json.decodeFromString<ArtistShortDto>(artist3Json).id
        val artist4Id = Json.decodeFromString<ArtistShortDto>(artist4Json).id

        val timetablePerformance1 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id, artist2Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        val timetablePerformance2 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        val timetablePerformance3 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist3Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        val timetablePerformance4 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist4Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        val timetablePerformance5 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id
            ),
            sceneId = scene1Id,
            typeOfPerformance = "aaa",
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 0, 45, 0, 0, ZoneOffset.ofHours(0)),
        )

        makePutRequest(
            mockMvc,
            "$eventEndpoint/$eventSavedId/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1,
                    timetablePerformance2,
                    timetablePerformance3,
                    timetablePerformance4,
                    timetablePerformance5
                )
            ),
            status().isOk
        )

        val lineupUpdatedJson = makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/lineup", status().isOk)
        val lineupUpdated = Json.decodeFromString<List<ArtistShortDto>>(lineupUpdatedJson)

        assertEquals(4, lineupUpdated.size)
        val setOfIds = mutableSetOf<Long>()
        lineupUpdated.forEach {
            setOfIds.add(it.id)
            when (it.id) {
                artist1Id -> {
                    assertEquals(artist1.name, it.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                artist2Id -> {
                    assertEquals(artist2.name, it.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                artist3Id -> {
                    assertEquals(artist3.name, it.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                artist4Id -> {
                    assertEquals(artist4.name, it.name)
                    assertEquals(0, it.overallFollowers)
                    assertEquals(0, it.weeklyFollowers)
                }
                else -> {
                    throw RuntimeException()
                }
            }
        }
        assertEquals(4, setOfIds.size)
    }


    @Test
    fun getTimetableAndAddTimetablePerformancesWithoutSceneAndDateTimesAndGetTimetableAgain() {
        val event1 = eventTest.copy(placeId = persistedPlace1Id)

        val eventJson = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        val eventSavedId = Json.decodeFromString<EventShortDto>(eventJson).id

        val timetableJson = makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/timetable", status().isOk)
        val timetable = Json.decodeFromString<List<TimetableForSceneDto>>(timetableJson)

        assertTrue(timetable.isEmpty())

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
            name = "Artist2"
        )
        val artist3 = artist1.copy(
            name = "Artist3"
        )
        val artist4 = artist1.copy(
            name = "Artist4"
        )

        val artist1Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist1), status().isCreated)
        val artist2Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist2), status().isCreated)
        val artist3Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist3), status().isCreated)
        val artist4Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist4), status().isCreated)

        val artist1Id = Json.decodeFromString<ArtistShortDto>(artist1Json).id
        val artist2Id = Json.decodeFromString<ArtistShortDto>(artist2Json).id
        val artist3Id = Json.decodeFromString<ArtistShortDto>(artist3Json).id
        val artist4Id = Json.decodeFromString<ArtistShortDto>(artist4Json).id

        val timetablePerformance1 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id, artist2Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        val timetablePerformance2 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist3Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        val timetablePerformance3 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist4Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        makePutRequest(
            mockMvc,
            "$eventEndpoint/$eventSavedId/timetable",
            Json.encodeToString(listOf(timetablePerformance1, timetablePerformance2, timetablePerformance3)),
            status().isOk
        )

        val timetableUpdatedJson =
            makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/timetable", status().isOk)
        val timetableUpdated = Json.decodeFromString<List<TimetableForSceneDto>>(timetableUpdatedJson)

        assertTrue(timetableUpdated.isEmpty())
    }

    @Test
    fun addTimetablePerformancesAndGetFilledTimetable() {
        val event1 = eventTest.copy(placeId = persistedPlace1Id)

        val eventJson = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        val eventSavedId = Json.decodeFromString<EventShortDto>(eventJson).id

        val scene1 = SceneDto(
            id = null,
            name = "Scene1",
            imageLink = "sceneImage1",
            priority = 1,
        )

        val scene2 = SceneDto(
            id = null,
            name = "Scene2",
            imageLink = "sceneImage2",
            priority = 2,
        )

        val scene3 = SceneDto(
            id = null,
            name = "Scene3",
            imageLink = "sceneImage3",
            priority = 3,
        )

        makePutRequest(
            mockMvc,
            "/place/$persistedPlace1Id/scenes",
            Json.encodeToString(listOf(scene1, scene2, scene3)),
            status().isOk
        )
        val savedScenesJson = makeGetRequest(mockMvc, "/place/public/$persistedPlace1Id/scenes", status().isOk)
        val savedScenes = Json.decodeFromString<List<SceneDto>>(savedScenesJson)

        val scene1Id = savedScenes.find { it.name == scene1.name }!!.id
        val scene2Id = savedScenes.find { it.name == scene2.name }!!.id
        val scene3Id = savedScenes.find { it.name == scene3.name }!!.id

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
            name = "Artist2"
        )
        val artist3 = artist1.copy(
            name = "Artist3"
        )
        val artist4 = artist1.copy(
            name = "Artist4"
        )

        val artist1Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist1), status().isCreated)
        val artist2Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist2), status().isCreated)
        val artist3Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist3), status().isCreated)
        val artist4Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist4), status().isCreated)

        val artist1Id = Json.decodeFromString<ArtistShortDto>(artist1Json).id
        val artist2Id = Json.decodeFromString<ArtistShortDto>(artist2Json).id
        val artist3Id = Json.decodeFromString<ArtistShortDto>(artist3Json).id
        val artist4Id = Json.decodeFromString<ArtistShortDto>(artist4Json).id

        val timetablePerformance1 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id, artist2Id
            ),
            sceneId = scene1Id,
            typeOfPerformance = "back 2 back",
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 2, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        val timetablePerformance2 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist3Id
            ),
            sceneId = scene1Id,
            typeOfPerformance = "dj set",
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 2, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 4, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        val timetablePerformance3 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist4Id
            ),
            sceneId = scene2Id,
            typeOfPerformance = null,
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 0, 45, 0, 0, ZoneOffset.ofHours(0)),
        )

        val timetablePerformance4 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id
            ),
            sceneId = scene3Id,
            typeOfPerformance = null,
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 1, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        val timetablePerformance5 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist2Id
            ),
            sceneId = scene3Id,
            typeOfPerformance = null,
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 1, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 2, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        val timetablePerformance6 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist3Id
            ),
            sceneId = scene3Id,
            typeOfPerformance = null,
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 2, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 3, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        val timetablePerformance7 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist4Id
            ),
            sceneId = scene3Id,
            typeOfPerformance = null,
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 3, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 4, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        makePutRequest(
            mockMvc, "$eventEndpoint/$eventSavedId/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1, timetablePerformance2, timetablePerformance3,
                    timetablePerformance4, timetablePerformance5, timetablePerformance6, timetablePerformance7
                )
            ),
            status().isOk
        )

        val timetableUpdatedJson =
            makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/timetable", status().isOk)
        val timetableUpdated = Json.decodeFromString<List<TimetableForSceneDto>>(timetableUpdatedJson)

        assertEquals(3, timetableUpdated.size)
        timetableUpdated.forEachIndexed { index, timetableForSceneDto ->
            if (index == 0) {
                assertEquals(scene3.name, timetableForSceneDto.scene!!.name)
                assertEquals(4, timetableForSceneDto.performances.size)
                timetableForSceneDto.performances.forEachIndexed { indexPerformance, it ->
                    if (indexPerformance == 0) {
                        assertEquals(timetablePerformance4.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance4.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance4.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance4.typeOfPerformance, it.typeOfPerformance)
                    } else if (indexPerformance == 1) {
                        assertEquals(timetablePerformance5.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance5.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance5.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance5.typeOfPerformance, it.typeOfPerformance)
                    } else if (indexPerformance == 2) {
                        assertEquals(timetablePerformance6.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance6.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance6.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance6.typeOfPerformance, it.typeOfPerformance)
                    } else if (indexPerformance == 3) {
                        assertEquals(timetablePerformance7.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance7.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance7.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance7.typeOfPerformance, it.typeOfPerformance)
                    }
                }
            } else if (index == 1) {
                assertEquals(scene2.name, timetableForSceneDto.scene!!.name)
                assertEquals(1, timetableForSceneDto.performances.size)
                timetableForSceneDto.performances.forEachIndexed { indexPerformance, it ->
                    if (indexPerformance == 0) {
                        assertEquals(timetablePerformance3.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance3.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance3.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance3.typeOfPerformance, it.typeOfPerformance)
                    }
                }
            } else if (index == 2) {
                assertEquals(scene1.name, timetableForSceneDto.scene!!.name)
                assertEquals(2, timetableForSceneDto.performances.size)
                timetableForSceneDto.performances.forEachIndexed { indexPerformance, it ->
                    if (indexPerformance == 0) {
                        assertEquals(timetablePerformance1.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance1.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance1.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance1.typeOfPerformance, it.typeOfPerformance)
                    } else if (indexPerformance == 1) {
                        assertEquals(timetablePerformance2.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance2.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance2.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance2.typeOfPerformance, it.typeOfPerformance)
                    }
                }
            }
        }
    }

    @Test
    fun addTimetablePerformanceAndUpdateWithSameValue() {
        val event1 = eventTest.copy(placeId = persistedPlace1Id)

        val eventJson = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        val eventSavedId = Json.decodeFromString<EventShortDto>(eventJson).id

        val scene1 = SceneDto(
            id = null,
            name = "Scene1",
            imageLink = "sceneImage1",
            priority = 1,
        )

        makePutRequest(
            mockMvc,
            "/place/$persistedPlace1Id/scenes",
            Json.encodeToString(listOf(scene1)),
            status().isOk
        )
        val savedScenesJson = makeGetRequest(mockMvc, "/place/public/$persistedPlace1Id/scenes", status().isOk)
        val savedScenes = Json.decodeFromString<List<SceneDto>>(savedScenesJson)
        val scene1Id = savedScenes.find { it.name == scene1.name }!!.id

        val artist1 = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudUsername = "soundcloud",
            instagramUsername = "instagram",
            about = "About Amelie",
            countryName = countryBeTest.name,
        )

        val artist1Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist1), status().isCreated)
        val artist1Id = Json.decodeFromString<ArtistShortDto>(artist1Json).id

        val timetablePerformance1 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id
            ),
            sceneId = scene1Id,
            typeOfPerformance = "back 2 back",
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 2, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        makePutRequest(
            mockMvc, "$eventEndpoint/$eventSavedId/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1
                )
            ),
            status().isOk
        )

        val timetableUpdatedJson =
            makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/timetable", status().isOk)
        val timetableUpdated = Json.decodeFromString<List<TimetableForSceneDto>>(timetableUpdatedJson)

        assertEquals(1, timetableUpdated.size)

        val timetablePerformance1Id =
            timetableUpdated[0].performances.find { it.startingDateTime == timetablePerformance1.startingDateTime }!!.id

        val timetablePerformance1Updated =
            timetablePerformance1.copy(id = timetablePerformance1Id)

        makePutRequest(
            mockMvc, "$eventEndpoint/$eventSavedId/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1Updated
                )
            ),
            status().isOk
        )

        val timetableUpdated2Json =
            makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/timetable", status().isOk)
        val timetableUpdated2 = Json.decodeFromString<List<TimetableForSceneDto>>(timetableUpdated2Json)

        assertEquals(1, timetableUpdated2.size)
        assertEquals(1, timetableUpdated2[0].performances.size)

        assertEquals(timetablePerformance1Id, timetableUpdated2[0].performances[0].id)
        assertEquals(timetablePerformance1.artistIds.size, timetableUpdated2[0].performances[0].artists.size)
        assertEquals(timetablePerformance1.startingDateTime, timetableUpdated2[0].performances[0].startingDateTime)
        assertEquals(timetablePerformance1.endingDateTime, timetableUpdated2[0].performances[0].endingDateTime)
        assertEquals(timetablePerformance1.typeOfPerformance, timetableUpdated2[0].performances[0].typeOfPerformance)
    }

    @Test
    fun addTimetablePerformancesAndUpdateTimetablePerformancesAndGetAgain() {
        val event1 = eventTest.copy(placeId = persistedPlace1Id)

        val eventJson = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event1), status().isCreated)
        val eventSavedId = Json.decodeFromString<EventShortDto>(eventJson).id

        val scene1 = SceneDto(
            id = null,
            name = "Scene1",
            imageLink = "sceneImage1",
            priority = 1,
        )

        makePutRequest(
            mockMvc,
            "/place/$persistedPlace1Id/scenes",
            Json.encodeToString(listOf(scene1)),
            status().isOk
        )
        val savedScenesJson = makeGetRequest(mockMvc, "/place/public/$persistedPlace1Id/scenes", status().isOk)
        val savedScenes = Json.decodeFromString<List<SceneDto>>(savedScenesJson)
        val scene1Id = savedScenes.find { it.name == scene1.name }!!.id

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
            name = "Artist2"
        )

        val artist1Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist1), status().isCreated)
        val artist2Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist2), status().isCreated)

        val artist1Id = Json.decodeFromString<ArtistShortDto>(artist1Json).id
        val artist2Id = Json.decodeFromString<ArtistShortDto>(artist2Json).id

        val timetablePerformance1 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id, artist2Id
            ),
            sceneId = scene1Id,
            typeOfPerformance = "back 2 back",
            startingDateTime = OffsetDateTime.of(2021, 8, 17, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 17, 2, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        val timetablePerformance2 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist2Id
            ),
            sceneId = scene1Id,
            typeOfPerformance = "dj set",
            startingDateTime = OffsetDateTime.of(2021, 8, 18, 2, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 18, 4, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        val timetablePerformance3 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist2Id
            ),
            sceneId = scene1Id,
            typeOfPerformance = null,
            startingDateTime = OffsetDateTime.of(2021, 8, 19, 1, 0, 0, 0, ZoneOffset.ofHours(0)),
            endingDateTime = OffsetDateTime.of(2021, 8, 19, 2, 0, 0, 0, ZoneOffset.ofHours(0)),
        )

        makePutRequest(
            mockMvc, "$eventEndpoint/$eventSavedId/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1, timetablePerformance2, timetablePerformance3
                )
            ),
            status().isOk
        )

        val timetableUpdatedJson =
            makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/timetable", status().isOk)
        val timetableUpdated = Json.decodeFromString<List<TimetableForSceneDto>>(timetableUpdatedJson)

        assertEquals(1, timetableUpdated.size)

        val timetablePerformance1Id =
            timetableUpdated[0].performances.find { it.startingDateTime == timetablePerformance1.startingDateTime }!!.id
        val timetablePerformance2Id =
            timetableUpdated[0].performances.find { it.startingDateTime == timetablePerformance2.startingDateTime }!!.id
        val timetablePerformance3Id =
            timetableUpdated[0].performances.find { it.startingDateTime == timetablePerformance3.startingDateTime }!!.id

        val timetablePerformance1Updated =
            timetablePerformance1.copy(id = timetablePerformance1Id)
        val timetablePerformance2Updated =
            timetablePerformance2.copy(id = timetablePerformance2Id, artistIds = setOf(artist1Id))
        val timetablePerformance4New = timetablePerformance3.copy(artistIds = setOf(artist1Id, artist2Id))

        makePutRequest(
            mockMvc, "$eventEndpoint/$eventSavedId/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1Updated, timetablePerformance2Updated, timetablePerformance4New
                )
            ),
            status().isOk
        )

        val timetableUpdated2Json =
            makeGetRequest(mockMvc, "$eventEndpoint/public/$eventSavedId/timetable", status().isOk)
        val timetableUpdated2 = Json.decodeFromString<List<TimetableForSceneDto>>(timetableUpdated2Json)

        assertEquals(1, timetableUpdated2.size)

        timetableUpdated2.forEachIndexed { index, timetableForSceneDto ->
            if (index == 0) {
                assertEquals(scene1.name, timetableForSceneDto.scene!!.name)
                assertEquals(3, timetableForSceneDto.performances.size)
                timetableForSceneDto.performances.forEachIndexed { indexPerformance, it ->
                    assertNotNull(it.id)
                    if (indexPerformance == 0) {
                        assertEquals(timetablePerformance1Updated.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance1Updated.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance1Updated.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance1Updated.typeOfPerformance, it.typeOfPerformance)
                    } else if (indexPerformance == 1) {
                        assertEquals(timetablePerformance2Updated.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance2Updated.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance2Updated.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance2Updated.typeOfPerformance, it.typeOfPerformance)
                    } else if (indexPerformance == 2) {
                        assertEquals(timetablePerformance4New.artistIds, it.artists.map { it.id }.toSet())
                        assertEquals(timetablePerformance4New.startingDateTime, it.startingDateTime)
                        assertEquals(timetablePerformance4New.endingDateTime, it.endingDateTime)
                        assertEquals(timetablePerformance4New.typeOfPerformance, it.typeOfPerformance)
                    }
                }
            }
        }
    }


    @Test
    fun getRelevantEventsByDate() {
        val now = OffsetDateTime.now(ZoneOffset.ofHours(0)).withHour(0)
        Mockito.doReturn(now).`when`(dateTimeProvider).getNow()

        val event1 = eventTest.copy(placeId = persistedPlace1Id)

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
        )

        val event5 = event1.copy(
            name = "Event5",
            startDateTime = now.plusDays(1).plusHours(1),
            endDateTime = now.plusDays(1).plusHours(3)
        )

        val event6 = event1.copy(
            name = "Event6",
            startDateTime = now.plusDays(85),
            endDateTime = now.plusDays(85).plusHours(2)
        )

        val event7 = event1.copy(
            name = "Event7",
            ticketsLink = null,
            startDateTime = now.plusDays(89).plusHours(23),
            endDateTime = now.plusDays(91).plusHours(2)
        )

        val event8 = event1.copy(
            name = "Event8",
            ticketsLink = null,
            startDateTime = now.plusDays(90).plusHours(1),
            endDateTime = now.plusDays(104).plusHours(2),
            placeId = persistedPlace2Id
        )

        val event9 = event1.copy(
            name = "Event9",
            startDateTime = now.plusDays(120),
            endDateTime = now.plusDays(121).plusHours(2)
        )

        val event10 = event1.copy(
            name = "Event10",
            startDateTime = now.minusDays(3),
            endDateTime = now.plusDays(3)
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
        val event10Json = makePostRequest(mockMvc, eventEndpoint, Json.encodeToString(event10), status().isCreated)

        val event1Id = Json.decodeFromString<EventShortDto>(event1Json).id
        val event2Id = Json.decodeFromString<EventShortDto>(event2Json).id
        val event3Id = Json.decodeFromString<EventShortDto>(event3Json).id
        val event4Id = Json.decodeFromString<EventShortDto>(event4Json).id
        val event5Id = Json.decodeFromString<EventShortDto>(event5Json).id
        val event6Id = Json.decodeFromString<EventShortDto>(event6Json).id
        val event7Id = Json.decodeFromString<EventShortDto>(event7Json).id
        val event8Id = Json.decodeFromString<EventShortDto>(event8Json).id
        val event9Id = Json.decodeFromString<EventShortDto>(event9Json).id
        val event10Id = Json.decodeFromString<EventShortDto>(event10Json).id

        val cityName = place1.cityName

        val relevantEventsByDateJson = makeGetRequest(
            mockMvc,
            "$eventEndpoint/public/relevantByDate?cityName=$cityName",
            status().isOk
        )
        val relevantEventsByDate = Json.decodeFromString<List<EventShortDto>>(relevantEventsByDateJson)

        assertEquals(7, relevantEventsByDate.size)

        relevantEventsByDate.forEachIndexed { index, event ->
            when (index) {
                0 -> {
                    assertEquals(event10Id, event.id)
                    assertEquals(event10.name, event.name)
                    assertEquals(EventStatus.LIVE, event.status)
                    assertEquals(event10.startDateTime.toLocalDateTime(), event.startDateTime.toLocalDateTime())
                }
                1 -> {
                    assertEquals(event2Id, event.id)
                    assertEquals(event2.name, event.name)
                    assertEquals(EventStatus.LIVE, event.status)
                    assertEquals(event2.startDateTime.toLocalDateTime(), event.startDateTime.toLocalDateTime())
                }
                2 -> {
                    assertEquals(event3Id, event.id)
                    assertEquals(event3.name, event.name)
                    assertEquals(EventStatus.TODAY, event.status)
                    assertEquals(event3.startDateTime.toLocalDateTime(), event.startDateTime.toLocalDateTime())
                }
                3 -> {
                    assertEquals(event4Id, event.id)
                    assertEquals(event4.name, event.name)
                    assertEquals(EventStatus.TOMORROW, event.status)
                    assertEquals(event4.startDateTime.toLocalDateTime(), event.startDateTime.toLocalDateTime())
                }
                4 -> {
                    assertEquals(event5Id, event.id)
                    assertEquals(event5.name, event.name)
                    assertEquals(EventStatus.TOMORROW, event.status)
                    assertEquals(event5.startDateTime.toLocalDateTime(), event.startDateTime.toLocalDateTime())
                }
                5 -> {
                    assertEquals(event6Id, event.id)
                    assertEquals(event6.name, event.name)
                    assertEquals(EventStatus.PRESALE, event.status)
                    assertEquals(event6.startDateTime.toLocalDateTime(), event.startDateTime.toLocalDateTime())
                }
                6 -> {
                    assertEquals(event7Id, event.id)
                    assertEquals(event7.name, event.name)
                    assertEquals(EventStatus.UPCOMING, event.status)
                    assertEquals(event7.startDateTime.toLocalDateTime(), event.startDateTime.toLocalDateTime())
                }
            }
        }
    }


    @Test
    fun getRelevantEventsOfArtist() {
        val now = OffsetDateTime.now(ZoneOffset.ofHours(0)).withHour(0)
        Mockito.doReturn(now).`when`(dateTimeProvider).getNow()

        val event1 = eventTest.copy(placeId = persistedPlace1Id)

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

        val artist1 = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            soundcloudUsername = "soundcloud",
            instagramUsername = "instagram",
            about = "About Amelie",
            countryName = countryBeTest.name,
        )

        val artist1Json =
            makePostRequest(mockMvc, "/artist", Json.encodeToString(artist1), status().isCreated)

        val artist1Id = Json.decodeFromString<ArtistShortDto>(artist1Json).id

        val scene1 = SceneDto(
            id = null,
            name = "Scene1",
            imageLink = "sceneImage1",
            priority = 1,
        )

        makePutRequest(
            mockMvc,
            "/place/$persistedPlace1Id/scenes",
            Json.encodeToString(listOf(scene1)),
            status().isOk
        )

        val savedScenesJson = makeGetRequest(mockMvc, "/place/public/$persistedPlace1Id/scenes", status().isOk)
        val savedScenes = Json.decodeFromString<List<SceneDto>>(savedScenesJson)

        val scene1Id = savedScenes.find { it.name == scene1.name }!!.id

        val timetablePerformance1 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id
            ),
            sceneId = null,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        val timetablePerformance2 = TimetablePerformanceWriteDto(
            id = null,
            artistIds = setOf(
                artist1Id
            ),
            sceneId = scene1Id,
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null,
        )

        makePutRequest(
            mockMvc, "$eventEndpoint/$event1Id/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1, timetablePerformance2,
                )
            ),
            status().isOk
        )

        makePutRequest(
            mockMvc, "$eventEndpoint/$event2Id/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1, timetablePerformance2,
                )
            ),
            status().isOk
        )

        makePutRequest(
            mockMvc, "$eventEndpoint/$event3Id/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1,
                )
            ),
            status().isOk
        )

        makePutRequest(
            mockMvc, "$eventEndpoint/$event4Id/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1,
                )
            ),
            status().isOk
        )

        makePutRequest(
            mockMvc, "$eventEndpoint/$event7Id/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1,
                )
            ),
            status().isOk
        )

        makePutRequest(
            mockMvc, "$eventEndpoint/$event9Id/timetable",
            Json.encodeToString(
                listOf(
                    timetablePerformance1,
                )
            ),
            status().isOk
        )

        val eventsOfArtistJson = makeGetRequest(
            mockMvc, "/artist/public/$artist1Id/events",
            status().isOk
        )
        val eventsOfArtist = Json.decodeFromString<List<EventShortDto>>(eventsOfArtistJson)

        assertEquals(5, eventsOfArtist.size)
        eventsOfArtist.forEachIndexed { index, eventShortDto ->
            when (index) {
                0 -> {
                    assertEquals(event2Id, eventShortDto.id)
                    assertEquals(event2.name, eventShortDto.name)
                }
                1 -> {
                    assertEquals(event3Id, eventShortDto.id)
                    assertEquals(event3.name, eventShortDto.name)
                }
                2 -> {
                    assertEquals(event4Id, eventShortDto.id)
                    assertEquals(event4.name, eventShortDto.name)
                }
                3 -> {
                    assertEquals(event7Id, eventShortDto.id)
                    assertEquals(event7.name, eventShortDto.name)
                }
                4 -> {
                    assertEquals(event9Id, eventShortDto.id)
                    assertEquals(event9.name, eventShortDto.name)
                }
            }
        }
    }

// todo test place's events

// todo test unity's events

}
