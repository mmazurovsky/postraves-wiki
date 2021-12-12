package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CoordinateDto
import com.postraves.backend.postraveswiki.data.dto.reading.PlaceFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.PlaceShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.SceneDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.PlaceWriteDto
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.followable.PlaceService
import com.postraves.backend.postraveswiki.utils.Components.customRedisProvider
import com.postraves.backend.postraveswiki.utils.Endpoints.cityEndpoint
import com.postraves.backend.postraveswiki.utils.Endpoints.countryEndpoint
import com.postraves.backend.postraveswiki.utils.Endpoints.placeEndpoint
import com.postraves.backend.postraveswiki.utils.MockAuthentication
import com.postraves.backend.postraveswiki.utils.Requests.makeDeleteRequest
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePutRequest
import com.postraves.backend.postraveswiki.utils.TestEntity.cityBrugesTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.placeBrugesTest
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
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlaceIntegrationTest(
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
    @Autowired
    private val mockMvc: MockMvc,
    @Autowired
    private val placeService: PlaceService,
    @Autowired
    private val countryService: CountryService,
    @Autowired
    private val cityService: CityService,
    @Qualifier("placeCountryQuickRepoImpl")
    private val placeCountryQuickRepoImpl: EntityCountryQuickRepo,
    @Qualifier("placeOverallFollowersQuickRepoImpl")
    private val placeOverallFollowersQuickRepoImpl: FollowersQuickRepo,
    @Qualifier("placeWeeklyFollowersQuickRepoImpl")
    private val placeWeeklyFollowersQuickRepoImpl: FollowersQuickRepo,
) : AbstractPostgresTest() {

    private val redisServer = RedisServer(customRedisProvider, redisPort)
    init {
        redisServer.start()
    }

    private val countryTest2 = countryBeTest.copy(
        name = "RU",
        phoneCode = "+7",
        
    )

    private val cityTest2 = cityBrugesTest.copy(
        name = "Antwerp",
        countryName = "BE",
        timeOffset = -3
    )

    private val cityTest3 = cityBrugesTest.copy(
        name = "Moscow",
        countryName = "RU",
        timeOffset = 0
    )

    private val sceneTest = SceneDto(
        id = null,
        name = "Scene1",
        imageLink = "sceneImage1",
        priority = 1,
    )

    @BeforeAll
    private fun createCountryForAssociations() {
        SecurityContextHolder.getContext().authentication = MockAuthentication.authAdminTest

        makePostRequest(mockMvc, countryEndpoint, Json.encodeToString(countryBeTest), status().isCreated)
        makePostRequest(mockMvc, countryEndpoint, Json.encodeToString(countryTest2), status().isCreated)
        makePostRequest(mockMvc, cityEndpoint, Json.encodeToString(cityBrugesTest), status().isCreated)
        makePostRequest(mockMvc, cityEndpoint, Json.encodeToString(cityTest2), status().isCreated)
        makePostRequest(mockMvc, cityEndpoint, Json.encodeToString(cityTest3), status().isCreated)
    }

    @AfterEach
    private fun cleanDb() = placeService.findAll().forEach { placeService.deleteById(it.id) }

    @AfterAll
    private fun cleanUp() {
        placeService.findAll().forEach { placeService.deleteById(it.id) }
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    fun savePlaceWithCityAssociation() {

        val placeToSave = placeBrugesTest

        val placeToSaveResponse =
            makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(placeToSave), status().isCreated)
        val placeId = Json.decodeFromString<PlaceShortDto>(placeToSaveResponse).id

        val placeSavedJson = makeGetRequest(mockMvc, "$placeEndpoint/public/$placeId", status().isOk)
        val placeSaved = Json.decodeFromString<PlaceFullDto>(placeSavedJson)

        val placesInCountryQuickRepo = placeCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val placesInOverallRating = placeOverallFollowersQuickRepoImpl.findTop(-1)
        val placesInWeeklyRating = placeWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertNotNull(placeSaved.id)
        assertEquals(placeToSave.name, placeSaved.name)
        assertEquals(0, placeSaved.overallFollowers)
        assertEquals(0, placeSaved.weeklyFollowers)
        assertEquals(placeToSave.imageLink, placeSaved.imageLink)
        assertEquals(placeToSave.soundcloudUsername, placeSaved.soundcloudUsername)
        assertEquals(placeToSave.instagramUsername, placeSaved.instagramUsername)
        assertEquals(placeToSave.about, placeSaved.about)
        assertEquals(placeToSave.cityName, placeSaved.city.name)
        assertEquals(countryBeTest.name, placeSaved.city.country.name)
        assertEquals(countryBeTest.phoneCode, placeSaved.city.country.phoneCode)
        assertNotNull(placeSaved.city.country.emojiCode)

        assert(placesInCountryQuickRepo.contains(placeSaved.id))
        assert(placesInOverallRating.contains(placeSaved.id))
        assert(placesInWeeklyRating.contains(placeSaved.id))
    }

    @Test
    fun updatePlaceAndCityAssociation() {

        val placeToSave = placeBrugesTest

        val responseSavedPlace =
            makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(placeToSave), status().isCreated)
        val savedId = Json.decodeFromString<PlaceShortDto>(responseSavedPlace).id

        val placeToUpdate = placeToSave.copy(
            id = savedId,
            name = "Club2",
            imageLink = "image2",
            soundcloudUsername = "soundcloud2",
            instagramUsername = "instagram2",
            about = "About club2",
            cityName = cityTest2.name
        )

        makePutRequest(mockMvc, placeEndpoint, Json.encodeToString(placeToUpdate), status().isOk)

        val updatedJson = makeGetRequest(mockMvc, "$placeEndpoint/public/$savedId", status().isOk)
        val updatedPlace = Json.decodeFromString<PlaceFullDto>(updatedJson)

        val placesInCountryQuickRepo = placeCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val placesInOverallRating = placeOverallFollowersQuickRepoImpl.findTop(-1)
        val placesInWeeklyRating = placeWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(placeToUpdate.id, updatedPlace.id)
        assertEquals(placeToUpdate.name, updatedPlace.name)
        assertEquals(0, updatedPlace.overallFollowers)
        assertEquals(0, updatedPlace.weeklyFollowers)
        assertEquals(placeToUpdate.imageLink, updatedPlace.imageLink)
        assertEquals(placeToUpdate.soundcloudUsername, updatedPlace.soundcloudUsername)
        assertEquals(placeToUpdate.instagramUsername, updatedPlace.instagramUsername)
        assertEquals(placeToUpdate.about, updatedPlace.about)
        assertEquals(cityTest2.name, updatedPlace.city.name)
        assertEquals(countryBeTest.name, updatedPlace.city.country.name)
        assertEquals(countryBeTest.phoneCode, updatedPlace.city.country.phoneCode)
        assertNotNull(updatedPlace.city.country.emojiCode)

        assert(placesInCountryQuickRepo.contains(updatedPlace.id))
        assert(placesInOverallRating.contains(updatedPlace.id))
        assert(placesInWeeklyRating.contains(updatedPlace.id))
    }

    @Test
    fun deletePlaceById() {

        val placeToSave = placeBrugesTest

        val responseSaved =
            makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(placeToSave), status().isCreated)
        val savedId = Json.decodeFromString<PlaceShortDto>(responseSaved).id

        makeDeleteRequest(mockMvc, "$placeEndpoint/$savedId", status().isOk)

        val responseFindAllPlacesJson = makeGetRequest(mockMvc, placeEndpoint, status().isOk)
        val responseFindAllPlaces = Json.decodeFromString<List<PlaceShortDto>>(responseFindAllPlacesJson)

        val placesInCountryQuickRepo = placeCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val placesInOverallRating = placeOverallFollowersQuickRepoImpl.findTop(-1)
        val placesInWeeklyRating = placeWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(0, responseFindAllPlaces.size)

        assert(!placesInCountryQuickRepo.contains(savedId))
        assert(!placesInOverallRating.contains(savedId))
        assert(!placesInWeeklyRating.contains(savedId))
    }

    @Test
    fun saveMultiplePlacesAndFindAll() {
        val place1 = placeBrugesTest

        val place2 = place1.copy(
            name = "Place2",
            imageLink = "image2",
            cityName = "Antwerp",
            about = "About2",
            instagramUsername = "instagram2",
            soundcloudUsername = "soundcloud2",
        )

        val place3 = place1.copy(
            name = "Place3",
            imageLink = "image3",
            cityName = "Antwerp",
            about = "About3",
            instagramUsername = "instagram3",
            soundcloudUsername = "soundcloud3",
        )

        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place1), status().isCreated)
        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place2), status().isCreated)
        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place3), status().isCreated)

        val responseAllPlacesJson = makeGetRequest(mockMvc, placeEndpoint, status().isOk)
        val responsePlaces = Json.decodeFromString<List<PlaceShortDto>>(responseAllPlacesJson)

        val placesInCountryQuickRepo = placeCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val placesInOverallRating = placeOverallFollowersQuickRepoImpl.findTop(-1)
        val placesInWeeklyRating = placeWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(3, responsePlaces.size)
        responsePlaces.forEach {
            assert(it.name == place1.name || it.name == place2.name || it.name == place3.name)
            if (it.name == place1.name) {
                assertEquals(place1.imageLink, it.imageLink)
                assertEquals(place1.cityName, it.city.name)
            } else if (it.name == place2.name) {
                assertEquals(place2.imageLink, it.imageLink)
                assertEquals(place2.cityName, it.city.name)
            }
        }
        
        assertEquals(3, placesInCountryQuickRepo.size)
        assertEquals(3, placesInOverallRating.size)
        assertEquals(3, placesInWeeklyRating.size)
    }

    @Test
    fun saveMultipleAndFindByName() {
        val place1 = placeBrugesTest

        val place2 = place1.copy(
            name = "Place2tis",
        )

        val place3 = place1.copy(
            name = "Placetis3",
        )

        val place4 = place1.copy(
            name = "Tis",
        )

        val place5 = place1.copy(
            name = "tiS",
            // INFO: Attention here
            isJustCity = true,
        )

        val place6 = place1.copy(
            name = "ti",
        )

        val place7 = place1.copy(
            name = "sit",
        )

        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place1), status().isCreated)
        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place2), status().isCreated)
        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place3), status().isCreated)
        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place4), status().isCreated)
        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place5), status().isCreated)
        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place6), status().isCreated)
        makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(place7), status().isCreated)

        val searchPhrase = "tis"
        val searchResults = makeGetRequest(mockMvc, "$placeEndpoint/public/search/$searchPhrase", status().isOk)
        val searchResultsDecoded = Json.decodeFromString<List<PlaceShortDto>>(searchResults)

        assertEquals(3, searchResultsDecoded.size)
        searchResultsDecoded.forEach {
            assert(it.name == place2.name ||
                    it.name == place3.name ||
                    it.name == place4.name)
        }
    }

    // todo check update country to another one in redis repo
    @Test
    fun updateCityOfAnotherCountryForPlace() {
        val placeToSave = placeBrugesTest

        val responseSavedPlace =
            makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(placeToSave), status().isCreated)
        val savedId = Json.decodeFromString<PlaceShortDto>(responseSavedPlace).id

        val placeToUpdate = placeToSave.copy(
            id = savedId,
            name = "Club2",
            imageLink = "image2",
            soundcloudUsername = "soundcloud2",
            instagramUsername = "instagram2",
            about = "About club2",
            cityName = cityTest3.name
        )

        makePutRequest(mockMvc, placeEndpoint, Json.encodeToString(placeToUpdate), status().isOk)

        val updatedJson = makeGetRequest(mockMvc, "$placeEndpoint/public/$savedId", status().isOk)
        val updatedPlace = Json.decodeFromString<PlaceFullDto>(updatedJson)

        val placesInCountryQuickRepoCountry1 = placeCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val placesInCountryQuickRepoCountry2 = placeCountryQuickRepoImpl.getAllIdsByCountry(countryTest2.name)
        val placesInOverallRating = placeOverallFollowersQuickRepoImpl.findTop(-1)
        val placesInWeeklyRating = placeWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(placeToUpdate.id, updatedPlace.id)
        assertEquals(cityTest3.name, updatedPlace.city.name)
        assertEquals(countryTest2.name, updatedPlace.city.country.name)
        assertEquals(countryTest2.phoneCode, updatedPlace.city.country.phoneCode)
        assertNotNull(updatedPlace.city.country.emojiCode)

        assert(!placesInCountryQuickRepoCountry1.contains(updatedPlace.id))
        assert(placesInCountryQuickRepoCountry2.contains(updatedPlace.id))
        assert(placesInOverallRating.contains(updatedPlace.id))
        assert(placesInWeeklyRating.contains(updatedPlace.id))
    }

    @Test
    fun findAllWhenEmpty() {
        val responseFindAllPlacesJson = makeGetRequest(mockMvc, placeEndpoint, status().isOk)
        val responseFindAllPlaces = Json.decodeFromString<List<PlaceShortDto>>(responseFindAllPlacesJson)
        assertEquals(0, responseFindAllPlaces.size)

        val placesInCountryQuickRepoCountry1 = placeCountryQuickRepoImpl.getAllIdsByCountry(countryBeTest.name)
        val placesInCountryQuickRepoCountry2 = placeCountryQuickRepoImpl.getAllIdsByCountry(countryTest2.name)
        val placesInOverallRating = placeOverallFollowersQuickRepoImpl.findTop(-1)
        val placesInWeeklyRating = placeWeeklyFollowersQuickRepoImpl.findTop(-1)

        assertEquals(0, placesInCountryQuickRepoCountry1.size)
        assertEquals(0, placesInCountryQuickRepoCountry2.size)
        assertEquals(0, placesInOverallRating.size)
        assertEquals(0, placesInWeeklyRating.size)
    }

    @Test
    fun savePlaceAndAddScenesToItAndGetAllScenesOfThePlace() {
        val placeToSave = placeBrugesTest

        val placeToSaveResponse =
            makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(placeToSave), status().isCreated)
        val placeId = Json.decodeFromString<PlaceShortDto>(placeToSaveResponse).id

        val savedScenesOfPlaceJson = makeGetRequest(mockMvc, "$placeEndpoint/public/$placeId/scenes", status().isOk)
        val savedScenesOfPlace = Json.decodeFromString<List<SceneDto>>(savedScenesOfPlaceJson)

        assertTrue(savedScenesOfPlace.isEmpty())

        val scene1ForPlace = sceneTest
        val scene2ForPlace = sceneTest.copy(
            name = "Scene2",
            imageLink = "sceneImage2",
            priority = 0
        )

        makePutRequest(mockMvc, "$placeEndpoint/$placeId/scenes", Json.encodeToString(listOf(scene1ForPlace, scene2ForPlace)), status().isOk)
        val savedScenesOfPlaceUpdatedJson = makeGetRequest(mockMvc, "$placeEndpoint/public/$placeId/scenes", status().isOk)
        val savedScenesOfPlaceUpdated = Json.decodeFromString<List<SceneDto>>(savedScenesOfPlaceUpdatedJson)

        assertEquals(2, savedScenesOfPlaceUpdated.size)

        assertEquals(scene1ForPlace.name, savedScenesOfPlaceUpdated[0].name)
        assertEquals(scene1ForPlace.imageLink, savedScenesOfPlaceUpdated[0].imageLink)
        assertEquals(scene1ForPlace.priority, savedScenesOfPlaceUpdated[0].priority)

        assertEquals(scene2ForPlace.name, savedScenesOfPlaceUpdated[1].name)
        assertEquals(scene2ForPlace.imageLink, savedScenesOfPlaceUpdated[1].imageLink)
        assertEquals(scene2ForPlace.priority, savedScenesOfPlaceUpdated[1].priority)
    }

    @Test
    fun savePlaceAndAddScenesToItAndDeletePlaceAndGetAllScenes() {
        val placeToSave = placeBrugesTest

        val placeToSaveResponse =
            makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(placeToSave), status().isCreated)
        val placeId = Json.decodeFromString<PlaceShortDto>(placeToSaveResponse).id

        val scene1ForPlace = sceneTest
        val scene2ForPlace = sceneTest.copy(
            name = "Scene2",
            imageLink = "sceneImage2",
            priority = 0
        )

        makePutRequest(mockMvc, "$placeEndpoint/$placeId/scenes", Json.encodeToString(listOf(scene1ForPlace, scene2ForPlace)), status().isOk)

        makeDeleteRequest(mockMvc, "$placeEndpoint/$placeId", status().isOk)

        val placesPreserved = placeService.findAll()
        val scenesPreserved = placeService.getAllScenes()

        assertEquals(0, placesPreserved.size)
        assertEquals(0, scenesPreserved.size)
    }

    @Test
    fun savePlaceAndAddScenesToItAndUpdateScenesAndGetScenes() {
        val placeToSave = placeBrugesTest

        val placeToSaveResponse =
            makePostRequest(mockMvc, placeEndpoint, Json.encodeToString(placeToSave), status().isCreated)
        val placeId = Json.decodeFromString<PlaceShortDto>(placeToSaveResponse).id

        val scene1ForPlace = sceneTest
        val scene2ForPlace = sceneTest.copy(
            name = "Scene2",
            imageLink = "sceneImage2",
            priority = 2
        )
        val scene3ForPlace = sceneTest.copy(
            name = "Scene3",
            imageLink = "sceneImage3",
            priority = 3
        )
        val scene4ForPlace = sceneTest.copy(
            name = "Scene4",
            imageLink = "sceneImage4",
            priority = 4
        )

        makePutRequest(mockMvc, "$placeEndpoint/$placeId/scenes", Json.encodeToString(listOf(scene1ForPlace, scene2ForPlace, scene3ForPlace, scene4ForPlace)), status().isOk)
        val savedScenesOfPlaceJson = makeGetRequest(mockMvc, "$placeEndpoint/public/$placeId/scenes", status().isOk)
        val savedScenesOfPlace = Json.decodeFromString<List<SceneDto>>(savedScenesOfPlaceJson)

        val scenes1And2 = savedScenesOfPlace.filter { it.name == scene1ForPlace.name || it.name == scene2ForPlace.name }

        assertEquals(4, savedScenesOfPlace.size)

        val scene1ForPlaceUpdated = sceneTest
            .copy(
                id = scenes1And2[1].id,
                priority = 100
            )
        val scene2ForPlaceUpdated = sceneTest
            .copy(
                id = scenes1And2[0].id,
                name = "Scene2Updated",
                imageLink = "sceneImage2Updated",
                priority = -1
        )

        makePutRequest(mockMvc, "$placeEndpoint/$placeId/scenes", Json.encodeToString(listOf(scene1ForPlaceUpdated, scene2ForPlaceUpdated)), status().isOk)
        val updatedScenesOfPlaceJson = makeGetRequest(mockMvc, "$placeEndpoint/public/$placeId/scenes", status().isOk)
        val updatedScenesOfPlace = Json.decodeFromString<List<SceneDto>>(updatedScenesOfPlaceJson)

        val allScenesPreserverAfterUpdate = placeService.getAllScenes()

        assertEquals(2, allScenesPreserverAfterUpdate.size)
        assertEquals(2, updatedScenesOfPlace.size)

        assertEquals(scene1ForPlaceUpdated.id, updatedScenesOfPlace[0].id)
        assertEquals(scene1ForPlaceUpdated.name, updatedScenesOfPlace[0].name)
        assertEquals(scene1ForPlaceUpdated.imageLink, updatedScenesOfPlace[0].imageLink)
        assertEquals(scene1ForPlaceUpdated.priority, updatedScenesOfPlace[0].priority)

        assertEquals(scene2ForPlaceUpdated.id, updatedScenesOfPlace[1].id)
        assertEquals(scene2ForPlaceUpdated.name, updatedScenesOfPlace[1].name)
        assertEquals(scene2ForPlaceUpdated.imageLink, updatedScenesOfPlace[1].imageLink)
        assertEquals(scene2ForPlaceUpdated.priority, updatedScenesOfPlace[1].priority)
    }
}
