package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CityIntegrationTest(
    @Autowired private val cityService: CityService,
    @Autowired private val countryService: CountryService,
    @Autowired private val mockMvc: MockMvc,
) : AbstractPostgresTest() {

    private val cityEndpoint: String = "/city"

    @BeforeAll
    private fun createCountriesForAssociations() {

        val country1 = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val country2 = CountryDto(
            name = "CHE",
            phoneCode = "+9",
            emojiCode = "CHECHE"
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
    private fun cleanAll() {
        cityService.findAll().forEach { cityService.deleteByName(it.name) }
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
    }

    @Test
    fun saveCityWithCountryAssociation() {

        val city = CityWriteDto(
            name = "Brugge",
            countryName = "BE",
            timeOffset = -3
        )

        val cityJson = Json.encodeToString(city)

        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isCreated)

        val response = Requests.makeGetRequest(mockMvc, "$cityEndpoint/public/Brugge", status().isOk)
        val responseDecoded = Json.decodeFromString<CityDto>(response)

        assertEquals(city.name, responseDecoded.name)
        assertEquals(city.countryName, responseDecoded.country.name)
    }

    @Test
    fun updateCityWithNewCountryAssociation() {

        val city = CityWriteDto(
            name = "Brugge",
            countryName = "BE",
            timeOffset = -3
        )

        val cityJson = Json.encodeToString(city)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isCreated)

        val cityUpdated = city.copy(countryName = "CHE")
        val cityUpdJson = Json.encodeToString(cityUpdated)
        Requests.makePutRequest(mockMvc, "/city", cityUpdJson, status().isOk)

        val cityFinal = Requests.makeGetRequest(mockMvc, "$cityEndpoint/public/Brugge", status().isOk)
        val cityFinalDecoded = Json.decodeFromString<CityDto>(cityFinal)

        assertEquals(cityUpdated.name, cityFinalDecoded.name)
        assertEquals(cityUpdated.countryName, cityFinalDecoded.country.name)
        assertEquals("+9", cityFinalDecoded.country.phoneCode)
    }

    @Test
    fun saveCityAndDeleteByName() {

        val city = CityWriteDto(
            name = "Brugge",
            countryName = "BE",
            timeOffset = -3
        )

        val cityJson = Json.encodeToString(city)
        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isCreated)

        Requests.makeDeleteRequest(mockMvc, "$cityEndpoint/Brugge", status().isOk)

        val cityListJson = Requests.makeGetRequest(mockMvc, cityEndpoint, status().isOk)
        val cityListDecoded = Json.decodeFromString<List<CityDto>>(cityListJson)

        assertEquals(0, cityListDecoded.size)
    }

    @Test
    fun saveMultipleAndFindAll() {

        val city1 = CityWriteDto(
            name = "Brugge",
            countryName = "BE",
            timeOffset = -3
        )

        val city2 = CityWriteDto(
            name = "Ant",
            countryName = "CHE",
            timeOffset = -1
        )

        val city3 = CityWriteDto(
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

        val cityListJson = Requests.makeGetRequest(mockMvc, cityEndpoint, status().isOk)

        val cityListDecoded = Json.decodeFromString<List<CityDto>>(cityListJson)

        assertEquals(3, cityListDecoded.size)
    }

    @Test
    fun tryToSaveCityWithoutCountryRefShouldBeBadRequest() {

        val city = mapOf(
            "name" to "Brugge",
            "timeOffset" to "-1"
        )

        val cityJson = Json.encodeToString(city)

        Requests.makePostRequest(mockMvc, cityEndpoint, cityJson, status().isBadRequest)

//        assertThrows<ArithmeticException> {
//            Requests.makePostRequest(mockMvc, "/city", cityJson, status().isBadRequest)
//        }
    }
}
