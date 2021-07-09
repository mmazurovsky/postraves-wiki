package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.service.ArtistService
import com.postraves.backend.postraveswiki.service.CountryService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtistIntegrationTest(
    @Autowired private val artistService : ArtistService,
    @Autowired private val countryService: CountryService,
    @Autowired private val mockMvc: MockMvc,
) {
//    @Autowired
//    private lateinit var flyway: Flyway
//
//    @BeforeAll
//    private fun migrate() {
//        flyway.clean()
//        flyway.migrate()
//    }

    @AfterEach
    private fun cleanDb() {
        artistService.findAll().forEach{artistService.deleteById(it.id)}
        countryService.findAll().forEach{countryService.deleteByName(it.name)}
    }

    fun makePostRequest(endpoint: String, bodyJson: String, expectedStatus: ResultMatcher): String {
        val mvcResult: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bodyJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(expectedStatus)
            .andReturn()
        return mvcResult.response.contentAsString
    }

    @Test
    fun saveArtist() {

        val country = CountryDto(
            name = "Russia",
            phoneCode = "+7",
            emojiCode = "RU"
        )

        val artist = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            countryName = "Russia",
            about = "About Amelie",
            instagramLink = "instagram",
            soundcloudLink = "soundcloud",
            soundcloudFollowersCount = 100
        )

        val countryJson = Json.encodeToString(country)
        val artistJson = Json.encodeToString(artist)

        val responseCountryJson = makePostRequest("/country", countryJson, status().isOk)
        val responseArtistJson = makePostRequest("/artist", artistJson, status().isOk)

        val responseCountry = Json.decodeFromString<CountryDto>(responseCountryJson)
        val responseArtist = Json.decodeFromString<ArtistFullDto>(responseArtistJson)

        Assertions.assertNotNull(responseArtist.id)
        Assertions.assertEquals("Amelie Lens", responseArtist.name)
        Assertions.assertEquals("Russia", responseCountry.name)
    }
}