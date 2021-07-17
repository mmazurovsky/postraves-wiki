package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CityDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.utils.Requests
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
    @Autowired private val mockMvc: MockMvc,
) : AbstractPostgresTest() {

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

    @Test
    fun saveCityWithCountryAssociation() {

        val city = CityWriteDto(
            name = "Brugge",
            countryName = "BE",
            timeOffset = -3
        )

        val cityJson = Json.encodeToString(city)

        val response = Requests.makePostRequest(mockMvc, "/city", cityJson, status().isCreated)

        val responseDecoded = Json.decodeFromString<CityDto>(response)

        assertEquals(city.name, responseDecoded.name)
        assertEquals(city.countryName, responseDecoded.country.name)
    }

    @Test
    fun updateCityWithCountryAssociation() {

        val city = CityWriteDto(
            name = "Brugge",
            countryName = "BE",
            timeOffset = -3
        )

        val cityJson = Json.encodeToString(city)
        Requests.makePostRequest(mockMvc, "/city", cityJson, status().isCreated)

        val cityUpdated = city.copy(countryName = "CHE")
        val cityUpdJson = Json.encodeToString(cityUpdated)
        Requests.makePutRequest(mockMvc, "/city", cityUpdJson, status().isOk)

        val cityFinal = Requests.makeGetRequest(mockMvc, "/city/public/Brugge", status().isOk)
        val cityFinalDecoded = Json.decodeFromString<CityDto>(cityFinal)

        assertEquals(cityUpdated.name, cityFinalDecoded.name)
        assertEquals(cityUpdated.countryName, cityFinalDecoded.country.name)
        assertEquals("+9", cityFinalDecoded.country.phoneCode)
    }

}