package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.repo.*
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.FollowersEnrichment.enrichWithFollowers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.min


interface ArtistService :
    BaseService<ArtistWriteDto, ArtistShortDto>,
    ByIdService<ArtistFullDto, ArtistShortDto>,
    RatingService<ArtistShortDto>,
    FindByName<ArtistShortDto> {
    fun enrichFullWithFollowers(artist: ArtistFullDto): ArtistFullDto
    fun enrichShortWithFollowers(artist: ArtistShortDto): ArtistShortDto
}

@Service
class ArtistServiceImpl(
    private val cityService: CityService,
    private val artistRepo: ArtistRepo,
    private val securityService: SecurityService,
    @Qualifier("artistCountryQuickRepoImpl")
    private val artistCountryRepo: QuickEntityCountryRepoAbstract,
    @Qualifier("artistWeeklyBestRepoImpl")
    private val weeklyBestRepo: WeeklyBestRepo,
    @Qualifier("artistWeeklyQuickFollowersRepoImpl")
    private val artistWeeklyFollowersDeltaRepo: QuickFollowersRepo,
    @Qualifier("artistOverallQuickFollowersRepoImpl")
    private val artistOverallFollowersImplRepo: QuickFollowersRepo,
) : ArtistService {

    @Autowired
    private lateinit var myUserProfileService: MyUserProfileService

    private fun findByIdDependingOnUser(id: Long): ArtistFullDto {
        val user = myUserProfileService.findMyProfile()
        return if (user == null)
            artistRepo.findById(id)
        else
            artistRepo.findByIdForUser(securityService.userAuthUid!!, id)
    }

    override fun findListByIds(ids: Set<Long>): List<ArtistShortDto> {
        return artistRepo.findListByIds(ids)
    }

    override fun enrichFullWithFollowers(artist: ArtistFullDto): ArtistFullDto {
        return enrichWithFollowers(artist, artistOverallFollowersImplRepo, artistWeeklyFollowersDeltaRepo)
    }

    override fun enrichShortWithFollowers(artist: ArtistShortDto): ArtistShortDto {
        return enrichWithFollowers(artist, artistOverallFollowersImplRepo, artistWeeklyFollowersDeltaRepo)
    }

    override fun findById(id: Long): ArtistFullDto {
        val foundArtist = findByIdDependingOnUser(id)
        return enrichFullWithFollowers(foundArtist)
    }

    override fun deleteById(id: Long) {
        // deleting form quick repo country
        val dtoToDelete = artistRepo.findById(id)
        val countryOfDtoToDelete = dtoToDelete.country?.name
        if (countryOfDtoToDelete != null) {
            artistCountryRepo.removeOneIdFromSet(countryOfDtoToDelete, id)
        }

        // deleting form quick repos ratings
        artistOverallFollowersImplRepo.removeId(id)
        artistWeeklyFollowersDeltaRepo.removeId(id)

        artistRepo.deleteById(id)
    }

    override fun save(dto: ArtistWriteDto): ArtistShortDto {
        val saved = artistRepo.save(dto)
        if (dto.countryName != null) artistCountryRepo.addOneIdToCountry(dto.countryName, saved.id)
        artistOverallFollowersImplRepo.setInitialFollowers(saved.id)
        artistWeeklyFollowersDeltaRepo.setInitialFollowers(saved.id)
        return saved
    }

    override fun update(dto: ArtistWriteDto) {
        // check country change and delete+add if necessary
        val previousCountryName = findByIdDependingOnUser(dto.id ?: throw TODO()).country?.name
        if (dto.countryName != previousCountryName) {
            if (dto.countryName != null) {
                artistCountryRepo.addOneIdToCountry(dto.countryName, dto.id)
            }
            if (previousCountryName != null) {
                artistCountryRepo.removeOneIdFromSet(previousCountryName, dto.id)
            }
        }

        artistRepo.update(dto)
    }

    // todo abstract method
    override fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        val countryName = cityService.findByName(cityName).country.name
        val artistFromTheCountryIds = artistCountryRepo.getAllIdsByCountry(countryName)
        val topArtistIdsAndScores = artistOverallFollowersImplRepo.findTop(-1)
        val topArtistFromTheCountryIdsAndScores =
            topArtistIdsAndScores.filterKeys { artistFromTheCountryIds.contains(it) }.toMap()
        // todo maybe order gets lost here
        val topArtistFromTheCountryIds = topArtistFromTheCountryIdsAndScores.keys
        val topArtistFromTheCountryDtos = findListByIds(topArtistFromTheCountryIds)
        val topArtistFromTheCountryDtosWithOverallFollowers = topArtistFromTheCountryDtos.map {
            it.copy(
                overallFollowers = topArtistFromTheCountryIdsAndScores[it.id] ?: TODO(),
                weeklyFollowers = artistWeeklyFollowersDeltaRepo.getFollowers(it.id)
            )
        }.toList()
        Collections.sort(
            topArtistFromTheCountryDtosWithOverallFollowers,
            Comparator.comparing { topArtistFromTheCountryIds.indexOf(it.id) })
        val result = topArtistFromTheCountryDtosWithOverallFollowers.subList(
            0,
            min(topArtistFromTheCountryDtosWithOverallFollowers.size, maxQuantity)
        )
        return result
    }

    // todo abstract method
    override fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        val countryName = cityService.findByName(cityName).country.name
        val artistFromTheCountryIds = artistCountryRepo.getAllIdsByCountry(countryName)
        val weeklyTopArtistIdsAndScores = artistWeeklyFollowersDeltaRepo.findTop(-1)
        val weeklyTopArtistFromTheCountryIdsAndScores =
            weeklyTopArtistIdsAndScores.filterKeys { artistFromTheCountryIds.contains(it) }.toMap()
        // todo maybe order gets lost here
        val weeklyTopArtistFromTheCountryIds = weeklyTopArtistIdsAndScores.keys
        val weeklyTopArtistFromTheCountryDtos = findListByIds(weeklyTopArtistFromTheCountryIds)
        val weeklyTopArtistDtosWithWeeklyFollowers = weeklyTopArtistFromTheCountryDtos.map {
            it.copy(
                weeklyFollowers = weeklyTopArtistFromTheCountryIdsAndScores[it.id] ?: TODO(),
                overallFollowers = artistOverallFollowersImplRepo.getFollowers(it.id)
            )
        }.toList()
        Collections.sort(
            weeklyTopArtistDtosWithWeeklyFollowers,
            Comparator.comparing { weeklyTopArtistFromTheCountryIds.indexOf(it.id) })
        val result = weeklyTopArtistDtosWithWeeklyFollowers.subList(
            0,
            min(weeklyTopArtistDtosWithWeeklyFollowers.size, maxQuantity)
        )
        return result
    }

    override fun findBestOfTheWeekByCityInCountry(cityName: String): ArtistShortDto {
        // todo test this
        val countryName = cityService.findByName(cityName).country.name
        val bestArtistAsString = Json.encodeToJsonElement(weeklyBestRepo.getWeeklyBestInCountry(countryName))
        val bestArtist = Json.decodeFromJsonElement<ArtistShortDto>(bestArtistAsString)
        return bestArtist
    }

    override fun incrementFollowers(id: Long) {
        if (securityService.userAuthUid != null) {
            artistOverallFollowersImplRepo.incrementFollowers(id)
            artistWeeklyFollowersDeltaRepo.incrementFollowers(id)
        }
    }

    override fun decrementFollowers(id: Long) {
        if (securityService.userAuthUid != null) {
            artistOverallFollowersImplRepo.decrementFollowers(id)
            artistWeeklyFollowersDeltaRepo.decrementFollowers(id)
        }
    }

    override fun findAll(): List<ArtistShortDto> {
        return artistRepo.findAll()
    }

    override fun findByPartOfName(namePart: String): List<ArtistShortDto> {
        return artistRepo.findByPartOfName(namePart)
    }
}