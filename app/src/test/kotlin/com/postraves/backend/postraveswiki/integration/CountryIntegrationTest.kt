package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.AbstractPostgresTest
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.utils.Requests
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import redis.embedded.RedisServer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CountryIntegrationTest(
    @Autowired
    private val countryService: CountryService,
    @Autowired
    private val mockMvc: MockMvc,
    @Value("\${spring.redis.port}")
    private val redisPort: Int,
    ) : AbstractPostgresTest() {

    private val countryEndpoint: String = "/country"
    private val redisServer = RedisServer(redisPort)

    private val countryTestData = CountryDto(
        name = "BE",
        nameRu = "NameRu",
        nameUk = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        phoneCode = "+7",
        emojiCode = null
    )

    init {
        redisServer.start()
    }

    @AfterEach
    private fun cleanDb() = countryService.findAll().forEach { countryService.deleteByName(it.name) }

    @AfterAll
    private fun cleanUp() {
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        redisServer.stop()
    }

    @Test
    fun saveCountry() {

        val countryToSave = countryTestData

        val countryJson = Json.encodeToString(countryToSave)
        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson, status().isCreated)

        val response = Requests.makeGetRequest(mockMvc, "$countryEndpoint/public/BE", status().isOk)
        val responseCountry = Json.decodeFromString<CountryDto>(response)

        assertEquals(countryToSave.name, responseCountry.name)
        assertEquals(countryToSave.phoneCode, responseCountry.phoneCode)
        assertNotNull(responseCountry.emojiCode)
    }

    @Test
    fun saveAndUpdateCountry() {

        val countryToSave = countryTestData

        val countryJson = Json.encodeToString(countryToSave)
        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson, status().isCreated)

        val countryToUpdate = countryToSave.copy(phoneCode = "+8", emojiCode = null)
        val countryUpdateJson = Json.encodeToString(countryToUpdate)
        Requests.makePutRequest(mockMvc, countryEndpoint, countryUpdateJson, status().isOk)

        val response = Requests.makeGetRequest(mockMvc, "$countryEndpoint/public/BE", status().isOk)
        val responseCountry = Json.decodeFromString<CountryDto>(response)

        assertEquals(countryToUpdate.name, responseCountry.name)
        assertEquals(countryToUpdate.phoneCode, responseCountry.phoneCode)
        assertNotNull(responseCountry.emojiCode)
    }

    @Test
    fun saveAndFindByName() {

        val countryToSave = countryTestData

        val countryJson = Json.encodeToString(countryToSave)
        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson, status().isCreated)

        val response = Requests.makeGetRequest(mockMvc, "$countryEndpoint/public/BE", status().isOk)
        val responseCountry = Json.decodeFromString<CountryDto>(response)

        assertEquals(countryToSave.name, responseCountry.name)
        assertEquals(countryToSave.phoneCode, responseCountry.phoneCode)
        assertNotNull(responseCountry.emojiCode)
    }

    @Test
    fun saveAndDeleteByName() {

        val countryToSave = countryTestData

        val countryJson = Json.encodeToString(countryToSave)
        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson, status().isCreated)

        Requests.makeDeleteRequest(mockMvc, "$countryEndpoint/BE", status().isOk)
        val response = Requests.makeGetRequest(mockMvc, countryEndpoint, status().isOk)
        val responseDecoded = Json.decodeFromString<List<CountryDto>>(response)
        assertEquals(0, responseDecoded.size)
    }

    @Test
    fun saveMultipleAndFindAll() {

        val countryToSave1 = countryTestData

        val countryToSave2 = countryToSave1.copy(
            name = "NI",
            phoneCode = "+8",
            emojiCode = null
        )

        val countryToSave3 = countryToSave1.copy(
            name = "LUX",
            phoneCode = "+9",
            emojiCode = null
        )

        val countryJson1 = Json.encodeToString(countryToSave1)
        val countryJson2 = Json.encodeToString(countryToSave2)
        val countryJson3 = Json.encodeToString(countryToSave3)
        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson1, status().isCreated)
        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson2, status().isCreated)
        Requests.makePostRequest(mockMvc, countryEndpoint, countryJson3, status().isCreated)

        val response = Requests.makeGetRequest(mockMvc, countryEndpoint, status().isOk)
        val responseCountryList = Json.decodeFromString<List<CountryDto>>(response)

        assertEquals(3, responseCountryList.size)
    }
}