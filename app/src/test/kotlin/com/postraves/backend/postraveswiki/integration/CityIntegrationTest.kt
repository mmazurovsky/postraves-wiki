package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.MoneyCurrencyService
import com.postraves.backend.postraveswiki.utils.Components.customRedisProvider
import com.postraves.backend.postraveswiki.utils.Endpoints.cityEndpoint
import com.postraves.backend.postraveswiki.utils.MockAuthentication
import com.postraves.backend.postraveswiki.utils.Requests
import com.postraves.backend.postraveswiki.utils.TestEntity.cityBrugesTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryBeTest
import com.postraves.backend.postraveswiki.utils.TestEntity.countryRuTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import redis.embedded.RedisExecProvider
import redis.embedded.RedisServer
import redis.embedded.util.Architecture
import redis.embedded.util.OS
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

    private val redisServer = RedisServer(customRedisProvider, redisPort)
    init {
        redisServer.start()
    }

//    private val cityTestData = CityWriteDto(
//        name = "Bruges",
//        nameRu = "NameRu",
//        nameEn = "NameUk",
//        nameDe = "NameDe",
//        nameFr = "NameFr",
//        countryName = "BE",
//        timeOffset = -3
//    )

    @BeforeAll
    private fun createCountriesForAssociations() {

//        val countryBe = CountryWriteDto(
//            name = "BE",
//            nameRu = "NameRu",
//            nameEn = "NameUk",
//            nameDe = "NameDe",
//            nameFr = "NameFr",
//            phoneCode = "+7",
//            
//        )
//
//        val countryRu = CountryWriteDto(
//            name = countryRuTest.name,
//            nameRu = "NameRu",
//            nameEn = "NameUk",
//            nameDe = "NameDe",
//            nameFr = "NameFr",
//            phoneCode = "+9",
//            
//        )
        SecurityContextHolder.getContext().setAuthentication(MockAuthentication.authAdminTest)

        val countryJson1 = Json.encodeToString(countryBeTest)
        val countryJson2 = Json.encodeToString(countryRuTest)

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
    }

    @Test
    fun saveCityWithCountryAssociation() {

        val city = cityBrugesTest

        val cityJson = Json.encodeToString(city)

        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isCreated)

        val response = Requests.makeGetRequest(mockMvc, "$cityEndpoint/public/${city.name}", status().isOk)
        val responseDecoded = Json.decodeFromString<CityDto>(response)

        assertEquals(city.name, responseDecoded.name)
        assertEquals(city.countryName, responseDecoded.country.name)
    }

    @Test
    fun updateCityWithNewCountryAssociation() {

        val city = cityBrugesTest

        Requests.makePostRequest(mockMvc, cityEndpoint, Json.encodeToString(city), status().isCreated)

        val cityUpdated = city.copy(countryName = countryRuTest.name)
        Requests.makePutRequest(mockMvc, "/city", Json.encodeToString(cityUpdated), status().isOk)

        val cityFinal = Requests.makeGetRequest(mockMvc, "$cityEndpoint/public/${city.name}", status().isOk)
        val cityFinalDecoded = Json.decodeFromString<CityDto>(cityFinal)

        assertEquals(cityUpdated.name, cityFinalDecoded.name)
        assertEquals(cityUpdated.countryName, cityFinalDecoded.country.name)
        assertEquals(countryRuTest.phoneCode, cityFinalDecoded.country.phoneCode)
    }

    @Test
    fun saveCityAndDeleteByName() {

        val city = cityBrugesTest

        val cityJson = Json.encodeToString(city)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isCreated)

        Requests.makeDeleteRequest(mockMvc, "$cityEndpoint/${city.name}", status().isOk)

        val cityListJson = Requests.makeGetRequest(mockMvc, "$cityEndpoint/public/all", status().isOk)
        val cityListDecoded = Json.decodeFromString<List<CityDto>>(cityListJson)

        assertEquals(0, cityListDecoded.size)
    }

    @Test
    fun saveMultipleAndFindAll() {

        val city1 = cityBrugesTest

        val city2 = city1.copy(
            name = "Ant",
            countryName = countryRuTest.name,
            timeOffset = -1
        )

        val city3 = city1.copy(
            name = "Amst",
            countryName = countryRuTest.name,
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
}
