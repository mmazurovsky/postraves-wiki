package com.postraves.backend.postraveswiki.integration

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.service.ArtistService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.utils.Requests.makeDeleteRequest
import com.postraves.backend.postraveswiki.utils.Requests.makeGetRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePostRequest
import com.postraves.backend.postraveswiki.utils.Requests.makePutRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.event.annotation.BeforeTestExecution
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
class ArtistIntegrationTest(
    @Autowired private val artistService: ArtistService,
    @Autowired private val countryService: CountryService,
    @Autowired private val mockMvc: MockMvc,
) {

    companion object {
        @Container
        private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:latest")

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword)
        }
    }

//    @AfterEach
//    private fun cleanDb() {
//        artistService.findAll().forEach{artistService.deleteById(it.id)}
//        countryService.findAll().forEach{countryService.deleteByName(it.name)}
//    }

    private fun createCountryForAssociations() {

        val country = CountryDto(
            name = "BE",
            phoneCode = "+7",
            emojiCode = "EBE"
        )

        val countryJson = Json.encodeToString(country)
        val responseCountryJson = makePostRequest(mockMvc, "/country", countryJson, status().isCreated)
        val responseCountry = Json.decodeFromString<CountryDto>(responseCountryJson)

        //        Assertions.assertEquals("BE", responseCountry.name)
        //        Assertions.assertEquals("+7", responseCountry.phoneCode)
        //        Assertions.assertEquals("EBE", responseCountry.emojiCode)

    }

    @Test
    @Order(1)
    fun contextLoads() {
    }

    @Test
    @Order(2)
    fun saveArtistWithCountryAssociation() {
        createCountryForAssociations()

        val artist = ArtistWriteDto(
            id = null,
            name = "Amelie Lens",
            imageLink = "image",
            countryName = "BE",
            about = "About Amelie",
            instagramLink = "instagram",
            soundcloudLink = "soundcloud",
            soundcloudFollowersCount = 100
        )

        val artistJson = Json.encodeToString(artist)
        val responseArtistJson = makePostRequest(mockMvc, "/artist", artistJson, status().isCreated)
        val responseArtist = Json.decodeFromString<ArtistFullDto>(responseArtistJson)

        Assertions.assertNotNull(responseArtist.id)
        Assertions.assertEquals("Amelie Lens", responseArtist.name)
        Assertions.assertEquals(20, responseArtist.baseRating)
        Assertions.assertEquals(0, responseArtist.overallFollowersCount)
        Assertions.assertEquals("image", responseArtist.imageLink)
        Assertions.assertEquals("soundcloud", responseArtist.soundcloudLink)
        Assertions.assertEquals("instagram", responseArtist.instagramLink)
        Assertions.assertEquals("About Amelie", responseArtist.about)
        Assertions.assertEquals("BE", responseArtist.country?.name)
        Assertions.assertEquals("+7", responseArtist.country?.phoneCode)
        Assertions.assertEquals("EBE", responseArtist.country?.emojiCode)
    }

    @Test
    @Order(3)
    fun updateArtistAndDeleteCountryAssociation() {

//        val responseInitialArtistJson = makeGetRequest(mockMvc, "/artist", status().isOk)
//        val responseInitialArtist = Json.decodeFromString<List<ArtistShortDto>>(responseInitialArtistJson)

        val artistUpdate = ArtistWriteDto(
            id = 1,
            name = "Amelie Lens2",
            imageLink = "image2",
            countryName = null,
            about = "About Amelie2",
            instagramLink = "instagram2",
            soundcloudLink = "soundcloud2",
            // this must not impact base rating
            soundcloudFollowersCount = 100000
        )
        val artistUpdateJson = Json.encodeToString(artistUpdate)

        val responseUpdatedArtistJson = makePutRequest(mockMvc, "/artist", artistUpdateJson, status().isOk)
        val responseUpdatedArtist = Json.decodeFromString<ArtistFullDto>(responseUpdatedArtistJson)

        Assertions.assertEquals(1, responseUpdatedArtist.id)
        Assertions.assertEquals("Amelie Lens2", responseUpdatedArtist.name)
        Assertions.assertEquals(20, responseUpdatedArtist.baseRating)
        Assertions.assertEquals(0, responseUpdatedArtist.overallFollowersCount)
        Assertions.assertEquals("image2", responseUpdatedArtist.imageLink)
        Assertions.assertEquals("soundcloud2", responseUpdatedArtist.soundcloudLink)
        Assertions.assertEquals("instagram2", responseUpdatedArtist.instagramLink)
        Assertions.assertEquals("About Amelie2", responseUpdatedArtist.about)
        Assertions.assertNull(responseUpdatedArtist.country)
    }

    @Test
    @Order(4)
    fun deleteArtistById() {
        val responseDeletedArtistJson = makeDeleteRequest(mockMvc, "/artist/1", status().isOk)
        val responseDeletedArtist = Json.decodeFromString<ArtistFullDto>(responseDeletedArtistJson)

        Assertions.assertEquals(1, responseDeletedArtist.id)
        Assertions.assertEquals("Amelie Lens2", responseDeletedArtist.name)

        val responseFindArtistJson = makeGetRequest(mockMvc, "/artist", status().isOk)
        val responseFindArtist = Json.decodeFromString<List<ArtistShortDto>>(responseFindArtistJson)

        Assertions.assertEquals(0, responseFindArtist.size)
    }

    @Test
    @Order(5)
    fun saveMultipleArtistsWithoutCountriesAndFindAll() {
        val artist1 = ArtistWriteDto(
            id = null,
            name = "Artist1",
            imageLink = "image1",
            countryName = null,
            about = "About1",
            instagramLink = "instagram1",
            soundcloudLink = "soundcloud1",
            soundcloudFollowersCount = null
        )

        val artist2 = ArtistWriteDto(
            id = null,
            name = "Artist2",
            imageLink = "image2",
            countryName = null,
            about = "About2",
            instagramLink = "instagram2",
            soundcloudLink = "soundcloud2",
            soundcloudFollowersCount = null
        )

        val artist3 = ArtistWriteDto(
            id = null,
            name = "Artist3",
            imageLink = "image3",
            countryName = null,
            about = "About3",
            instagramLink = "instagram3",
            soundcloudLink = "soundcloud3",
            soundcloudFollowersCount = null
        )

        val artistJson1 = Json.encodeToString(artist1)
        val artistJson2 = Json.encodeToString(artist2)
        val artistJson3 = Json.encodeToString(artist3)
        val responseArtist1Json = makePostRequest(mockMvc, "/artist", artistJson1, status().isCreated)
        val responseArtist2Json = makePostRequest(mockMvc, "/artist", artistJson2, status().isCreated)
        val responseArtist3Json = makePostRequest(mockMvc, "/artist", artistJson3, status().isCreated)

        val responseArtist1 = Json.decodeFromString<ArtistFullDto>(responseArtist1Json)
        val responseArtist2 = Json.decodeFromString<ArtistFullDto>(responseArtist2Json)
        val responseArtist3 = Json.decodeFromString<ArtistFullDto>(responseArtist3Json)

        Assertions.assertNotNull(responseArtist1.id)
        Assertions.assertEquals(artist1.name, responseArtist1.name)
        Assertions.assertNull(responseArtist1.country)
        Assertions.assertNotNull(responseArtist2.id)
        Assertions.assertEquals(artist2.name, responseArtist2.name)
        Assertions.assertNull(responseArtist2.country)
        Assertions.assertNotNull(responseArtist3.id)
        Assertions.assertEquals(artist3.name, responseArtist3.name)
        Assertions.assertNull(responseArtist3.country)

        val responseArtistsJson = makeGetRequest(mockMvc, "/artist", status().isOk)
        val responseArtists = Json.decodeFromString<List<ArtistShortDto>>(responseArtistsJson)

        Assertions.assertEquals(3, responseArtists.size)
    }

    @Test
    @Order(6)
    fun findOverallRating() {
        val artist4 = ArtistWriteDto(
            id = null,
            name = "Artist4",
            imageLink = "image4",
            countryName = "BE",
            about = "About4",
            instagramLink = "instagram4",
            soundcloudLink = "soundcloud4",
            soundcloudFollowersCount = 1000
        )

        val artist5 = ArtistWriteDto(
            id = null,
            name = "Artist5",
            imageLink = "image5",
            countryName = "BE",
            about = "About5",
            instagramLink = "instagram5",
            soundcloudLink = "soundcloud5",
            soundcloudFollowersCount = 100
        )

        val artist6 = ArtistWriteDto(
            id = null,
            name = "Artist6",
            imageLink = "image6",
            countryName = "BE",
            about = "About6",
            instagramLink = "instagram6",
            soundcloudLink = "soundcloud6",
            soundcloudFollowersCount = 10
        )

        val artist7 = ArtistWriteDto(
            id = null,
            name = "Artist7",
            imageLink = "image7",
            countryName = "BE",
            about = "About7",
            instagramLink = "instagram7",
            soundcloudLink = "soundcloud7",
            soundcloudFollowersCount = null
        )

        val artistJson4 = Json.encodeToString(artist4)
        val artistJson5 = Json.encodeToString(artist5)
        val artistJson6 = Json.encodeToString(artist6)
        val artistJson7 = Json.encodeToString(artist7)

        val responseArtist4Json = makePostRequest(mockMvc, "/artist", artistJson4, status().isCreated)
        val responseArtist5Json = makePostRequest(mockMvc, "/artist", artistJson5, status().isCreated)
        val responseArtist6Json = makePostRequest(mockMvc, "/artist", artistJson6, status().isCreated)
        val responseArtist7Json = makePostRequest(mockMvc, "/artist", artistJson7, status().isCreated)

        val responseOverallRatingJson =
            makeGetRequest(mockMvc, "/artist/public/overallRating?cityName=BE&maxQuantity=10", status().isOk)
        val responseOverallRating = Json.decodeFromString<List<ArtistShortDto>>(responseOverallRatingJson)

        responseOverallRating.forEachIndexed { index, artistShortDto ->
            when (index) {
                0 -> {
                    Assertions.assertEquals(artist4.name, artistShortDto.name)
                    Assertions.assertEquals(200, artistShortDto.baseRating)
                    Assertions.assertEquals(0, artistShortDto.overallFollowersCount)
                }
                1 -> {
                    Assertions.assertEquals(artist5.name, artistShortDto.name)
                    Assertions.assertEquals(20, artistShortDto.baseRating)
                    Assertions.assertEquals(0, artistShortDto.overallFollowersCount)
                }
                2 -> {
                    Assertions.assertEquals(artist6.name, artistShortDto.name)
                    Assertions.assertEquals(2, artistShortDto.baseRating)
                    Assertions.assertEquals(0, artistShortDto.overallFollowersCount)
                }
                3 -> {
                    Assertions.assertEquals(artist7.name, artistShortDto.name)
                    Assertions.assertEquals(0, artistShortDto.baseRating)
                    Assertions.assertEquals(0, artistShortDto.overallFollowersCount)
                }
            }
        }
    }
}