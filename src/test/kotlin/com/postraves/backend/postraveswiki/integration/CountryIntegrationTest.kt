package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.utils.Requests
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
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
class CountryIntegrationTest(
    @Autowired private val countryService: CountryService,
    @Autowired private val mockMvc: MockMvc,
) : AbstractPostgresTest() {

    @AfterEach
    private fun cleanDb() = countryService.findAll().forEach { countryService.deleteByName(it.name) }

    @Test
    fun saveCountry() {

        val countryToSave = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val countryJson = Json.encodeToString(countryToSave)
        val response = Requests.makePostRequest(mockMvc, "/country", countryJson, status().isCreated)
        val responseCountry = Json.decodeFromString<CountryDto>(response)

        assertEquals(countryToSave.name, responseCountry.name)
        assertEquals(countryToSave.phoneCode, responseCountry.phoneCode)
        assertEquals(countryToSave.emojiCode, responseCountry.emojiCode)
    }

    @Test
    fun saveAndUpdateCountry() {

        val countryToSave = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val countryJson = Json.encodeToString(countryToSave)
        Requests.makePostRequest(mockMvc, "/country", countryJson, status().isCreated)

        val countryToUpdate = countryToSave.copy(phoneCode = "+8", emojiCode = "BEBE")
        val countryUpdateJson = Json.encodeToString(countryToUpdate)
        val response = Requests.makePutRequest(mockMvc, "/country", countryUpdateJson, status().isOk)
        val responseCountry = Json.decodeFromString<CountryDto>(response)

        assertEquals(countryToUpdate.name, responseCountry.name)
        assertEquals(countryToUpdate.phoneCode, responseCountry.phoneCode)
        assertEquals(countryToUpdate.emojiCode, responseCountry.emojiCode)
    }

    @Test
    fun saveAndFindByName() {

        val countryToSave = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val countryJson = Json.encodeToString(countryToSave)
        Requests.makePostRequest(mockMvc, "/country", countryJson, status().isCreated)

        val response = Requests.makeGetRequest(mockMvc, "/country/public/BE", status().isOk)
        val responseCountry = Json.decodeFromString<CountryDto>(response)

        assertEquals(countryToSave.name, responseCountry.name)
        assertEquals(countryToSave.phoneCode, responseCountry.phoneCode)
        assertEquals(countryToSave.emojiCode, responseCountry.emojiCode)
    }

    @Test
    fun saveAndDeleteByName() {

        val countryToSave = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val countryJson = Json.encodeToString(countryToSave)
        Requests.makePostRequest(mockMvc, "/country", countryJson, status().isCreated)

        Requests.makeDeleteRequest(mockMvc, "/country/BE", status().isOk)
        val response = Requests.makeGetRequest(mockMvc, "/country", status().isOk)
        val responseDecoded = Json.decodeFromString<List<CountryDto>>(response)
        assertEquals(0, responseDecoded.size)
    }

    @Test
    fun saveMultipleAndFindAll() {

        val countryToSave1 = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val countryToSave2 = CountryDto(
            name = "NI",
            phoneCode = "+8",
            emojiCode = "NIL"
        )

        val countryToSave3 = CountryDto(
            name = "LUX",
            phoneCode = "+9",
            emojiCode = "LUXE"
        )

        val countryJson1 = Json.encodeToString(countryToSave1)
        val countryJson2 = Json.encodeToString(countryToSave2)
        val countryJson3 = Json.encodeToString(countryToSave3)
        Requests.makePostRequest(mockMvc, "/country", countryJson1, status().isCreated)
        Requests.makePostRequest(mockMvc, "/country", countryJson2, status().isCreated)
        Requests.makePostRequest(mockMvc, "/country", countryJson3, status().isCreated)

        val response = Requests.makeGetRequest(mockMvc, "/country", status().isOk)
        val responseCountryList = Json.decodeFromString<List<CountryDto>>(response)

        assertEquals(3, responseCountryList.size)
    }
}