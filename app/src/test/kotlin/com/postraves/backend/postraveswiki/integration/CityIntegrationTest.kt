package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.utils.Requests
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import redis.embedded.RedisServer
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CityIntegrationTest(
    @Autowired
    private val cityService: CityService,
    @Autowired
    private val countryService: CountryService,
    @Autowired
    private val mockMvc: MockMvc,
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
    ) : AbstractPostgresTest() {

    private val cityEndpoint: String = "/city"
    private val redisServer = RedisServer(redisPort)

    init {
        redisServer.start()
    }

    private val cityTestData = CityWriteDto(
        name = "Bruges",
        nameRu = "NameRu",
        nameUk = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        countryName = "BE",
        timeOffset = -3
    )

    @BeforeAll
    private fun createCountriesForAssociations() {

        logger.info("City Integration Test started")

        val country1 = CountryDto(
            name = "BE",
            nameRu = "NameRu",
            nameUk = "NameUk",
            nameDe = "NameDe",
            nameFr = "NameFr",
            phoneCode = "+7",
            emojiCode = null
        )

        val country2 = CountryDto(
            name = "CHE",
            nameRu = "NameRu",
            nameUk = "NameUk",
            nameDe = "NameDe",
            nameFr = "NameFr",
            phoneCode = "+9",
            emojiCode = null
        )

        val countryJson1 = Json.encodeToString(country1)
        val countryJson2 = Json.encodeToString(country2)

        Requests.makePostRequest(mockMvc, "/country", countryJson1, status().isCreated)
        Requests.makePostRequest(mockMvc, "/country", countryJson2, status().isCreated)
    }

    @AfterEach
    private fun cleanDb() {
        cityService.findAll().forEach { cityService.deleteByName(it.name) }
    }

    @AfterAll
    private fun cleanUp() {
        cityService.findAll().forEach { cityService.deleteByName(it.name) }
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
        logger.info("City Integration Test ended")
    }

    @Test
    fun saveCityWithCountryAssociation() {

        val city = cityTestData

        val cityJson = Json.encodeToString(city)

        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isCreated)

        val response = Requests.makeGetRequest(mockMvc, "$cityEndpoint/public/Bruges", status().isOk)
        val responseDecoded = Json.decodeFromString<CityDto>(response)

        assertEquals(city.name, responseDecoded.name)
        assertEquals(city.countryName, responseDecoded.country.name)
    }

    @Test
    fun updateCityWithNewCountryAssociation() {

        val city = cityTestData

        val cityJson = Json.encodeToString(city)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isCreated)

        val cityUpdated = city.copy(countryName = "CHE")
        val cityUpdJson = Json.encodeToString(cityUpdated)
        Requests.makePutRequest(mockMvc, "/city", cityUpdJson, status().isOk)

        val cityFinal = Requests.makeGetRequest(mockMvc, "$cityEndpoint/public/Bruges", status().isOk)
        val cityFinalDecoded = Json.decodeFromString<CityDto>(cityFinal)

        assertEquals(cityUpdated.name, cityFinalDecoded.name)
        assertEquals(cityUpdated.countryName, cityFinalDecoded.country.name)
        assertEquals("+9", cityFinalDecoded.country.phoneCode)
    }

    @Test
    fun saveCityAndDeleteByName() {

        val city = cityTestData

        val cityJson = Json.encodeToString(city)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isCreated)

        Requests.makeDeleteRequest(mockMvc, "$cityEndpoint/Bruges", status().isOk)

        val cityListJson = Requests.makeGetRequest(mockMvc, "$cityEndpoint/public/all", status().isOk)
        val cityListDecoded = Json.decodeFromString<List<CityDto>>(cityListJson)

        assertEquals(0, cityListDecoded.size)
    }

    @Test
    fun saveMultipleAndFindAll() {

        val city1 = cityTestData

        val city2 = city1.copy(
            name = "Ant",
            countryName = "CHE",
            timeOffset = -1
        )

        val city3 = city1.copy(
            name = "Amst",
            countryName = "CHE",
            timeOffset = -1
        )

        val cityJson1 = Json.encodeToString(city1)
        val cityJson2 = Json.encodeToString(city2)
        val cityJson3 = Json.encodeToString(city3)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson1, status().isCreated)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson2, status().isCreated)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson3, status().isCreated)

        val cityListPublicEndpointJson = Requests.makeGetRequest(mockMvc, "$cityEndpoint/public/all", status().isOk)

        val cityListPublicEndpointDecoded = Json.decodeFromString<List<CityDto>>(cityListPublicEndpointJson)

        assertEquals(3, cityListPublicEndpointDecoded.size)
    }

    @Test
    fun tryToSaveCityWithoutCountryRefShouldBeBadRequest() {

        val city = mapOf(
            "name" to "Bruges",
            "timeOffset" to "-1"
        )

        val cityJson = Json.encodeToString(city)

        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isBadRequest)

//        assertThrows<ArithmeticException> {
//            Requests.makePostRequest(mockMvc, "/city", cityJson, status().isBadRequest)
//        }
    }
}
