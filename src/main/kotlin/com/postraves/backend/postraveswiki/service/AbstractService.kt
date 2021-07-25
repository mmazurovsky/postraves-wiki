package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseFullDtoWithIdAndRating
import com.postraves.backend.postraveswiki.data.dto.BaseRatingDtoWithId
import com.postraves.backend.postraveswiki.data.dto.BaseShortDtoWithIdAndRating
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.repo.*
import com.postraves.backend.postraveswiki.security.SecurityService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.math.min

abstract class AbstractService<WRITEDTO : BaseWriteDto,
        FULLDTO : BaseFullDtoWithIdAndRating<FULLDTO>,
        SHORTDTO : BaseShortDtoWithIdAndRating<SHORTDTO>,
        REPO>
    (
    private val cityService: CityService,
    private val securityService: SecurityService,
    private val entityCountryRepo: QuickEntityCountryRepoAbstract,
    private val weeklyBestRepo: WeeklyBestRepo,
    private val entityWeeklyFollowersRepo: QuickFollowersRepo,
    private val entityOverallFollowersRepo: QuickFollowersRepo,
    private val entityRepo: REPO,
) : BaseService<WRITEDTO, SHORTDTO>, FindByName<SHORTDTO>, ByIdService<FULLDTO, SHORTDTO>, RatingService<FULLDTO, SHORTDTO>
        where REPO : BaseRepo<WRITEDTO, SHORTDTO>,
              REPO : ByIdRepo<FULLDTO, SHORTDTO>,
              REPO : FindByNameRepo<SHORTDTO> {

    @Autowired
    private lateinit var myUserProfileService: MyUserProfileService

    private fun findByIdDependingOnUser(id: Long): FULLDTO {
        val user = myUserProfileService.findMyProfile()
        return if (user == null)
            entityRepo.findById(id)
        else
            entityRepo.findByIdForUser(securityService.userAuthUid!!, id)
    }

    override fun findListByIds(ids: Set<Long>): List<SHORTDTO> {
        return entityRepo.findListByIds(ids)
    }

    override fun findById(id: Long): FULLDTO {
        val foundArtist = findByIdDependingOnUser(id)
        return enrichWithFollowersCalculationRequired(foundArtist)
    }

    private fun calculateFollowers(id: Long): Pair<Int, Int> {
        val overallFollowers = entityOverallFollowersRepo.getFollowers(id)
        val weeklyFollowers = entityWeeklyFollowersRepo.getFollowers(id)
        return overallFollowers to weeklyFollowers
    }

    fun <T : BaseRatingDtoWithId<T>> enrichWithFollowersCalculationRequired(dto: T): T {
        val followers = calculateFollowers(dto.id)
        return dto.copyWithFollowersEnriched(followers.first, followers.second)
    }

    private fun <T : BaseRatingDtoWithId<T>> enrichWithFollowersWithoutCalculation(
        dto: T,
        overallFollowers: Int,
        weeklyFollowers: Int
    ): T {
        return dto.copyWithFollowersEnriched(overallFollowers, weeklyFollowers)
    }

    override fun deleteById(id: Long) {
        // deleting form quick repo country
        val dtoToDelete = entityRepo.findById(id)

        checkCountryAndRemoveFromCountryQuickRepo(dtoToDelete)

        // deleting form quick repos ratings
        entityOverallFollowersRepo.removeId(id)
        entityWeeklyFollowersRepo.removeId(id)

        entityRepo.deleteById(id)
    }

    abstract fun checkCountryAndRemoveFromCountryQuickRepo(dto: FULLDTO)

    override fun save(dto: WRITEDTO): SHORTDTO {
        val saved = entityRepo.save(dto)
        checkCountryAndAddToCountryQuickRepo(dto, saved.id)
        entityOverallFollowersRepo.setInitialFollowers(saved.id)
        entityWeeklyFollowersRepo.setInitialFollowers(saved.id)
        return saved
    }

    abstract fun checkCountryAndAddToCountryQuickRepo(dto: WRITEDTO, id: Long)

    override fun update(dto: WRITEDTO) {
        // check country change and delete+add if necessary
        checkCountryAndAddAndRemoveFromCountryQuickRepo(dto)
        entityRepo.update(dto)
    }

    abstract fun checkCountryAndAddAndRemoveFromCountryQuickRepo(dto: WRITEDTO)

    private fun findAbstractRatingForCityByCountry(
        mainFollowersRepo: QuickFollowersRepo,
        cityName: String
    ): Pair<List<SHORTDTO>, Map<Long, Int>> {
        val countryName = cityService.findByName(cityName).country.name
        val entitiesFromTheCountry = entityCountryRepo.getAllIdsByCountry(countryName)
        val topEntityIdsAndScores = mainFollowersRepo.findTop(-1)
        val topEntityIdsAndScoresFromTheCountry =
            topEntityIdsAndScores.filterKeys { entitiesFromTheCountry.contains(it) }.toMap()
        val topEntityFromTheCountryIds = topEntityIdsAndScoresFromTheCountry.keys
        val topEntityFromTheCountryDtos = findListByIds(topEntityFromTheCountryIds)
        // todo this is extra sorting
        Collections.sort(
            topEntityFromTheCountryDtos,
            Comparator.comparing { topEntityFromTheCountryIds.indexOf(it.id) })
        return topEntityFromTheCountryDtos to topEntityIdsAndScoresFromTheCountry
    }

    override fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO> {

        val resultFromAbstract = findAbstractRatingForCityByCountry(entityOverallFollowersRepo, cityName)
        val topEntityListWithoutFollowers = resultFromAbstract.first
        val topEntityIdsAndScoresFromTheCountry = resultFromAbstract.second
        val topEntitiesEnrichedWithFollowers = topEntityListWithoutFollowers.map {
            enrichWithFollowersWithoutCalculation(
                it,
                overallFollowers = topEntityIdsAndScoresFromTheCountry[it.id] ?: throw NotFoundException("Overall followers in city $cityName of entity", it.id.toString()),
                weeklyFollowers = entityWeeklyFollowersRepo.getFollowers(it.id)
            )
        }.toList()
        val result = topEntitiesEnrichedWithFollowers.subList(
            0,
            min(topEntitiesEnrichedWithFollowers.size, maxQuantity)
        )
        return result
    }

    override fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO> {
        val resultFromAbstract = findAbstractRatingForCityByCountry(entityWeeklyFollowersRepo, cityName)
        val topEntityListWithoutFollowers = resultFromAbstract.first
        val topEntityIdsAndScoresFromTheCountry = resultFromAbstract.second
        val topEntitiesEnrichedWithFollowers = topEntityListWithoutFollowers.map {
            enrichWithFollowersWithoutCalculation(
                it,
                overallFollowers = entityOverallFollowersRepo.getFollowers(it.id),
                weeklyFollowers = topEntityIdsAndScoresFromTheCountry[it.id] ?: throw NotFoundException("Weekly followers in city $cityName of entity", it.id.toString()),
            )
        }.toList()
        val result = topEntitiesEnrichedWithFollowers.subList(
            0,
            min(topEntitiesEnrichedWithFollowers.size, maxQuantity)
        )
        return result
    }

    override fun findBestOfTheWeekByCityInCountry(cityName: String): SHORTDTO {
        // todo test this
        val countryName = cityService.findByName(cityName).country.name
        val bestArtistAsString = Json.encodeToJsonElement(weeklyBestRepo.getWeeklyBestInCountry(countryName))
        val bestArtist = decodeShortDtoFromJson(bestArtistAsString)
        return bestArtist
    }

    abstract fun decodeShortDtoFromJson(encoded: JsonElement): SHORTDTO

    override fun incrementFollowers(id: Long) {
        if (securityService.userAuthUid != null) {
            entityOverallFollowersRepo.incrementFollowers(id)
            entityWeeklyFollowersRepo.incrementFollowers(id)
        }
    }

    override fun decrementFollowers(id: Long) {
        if (securityService.userAuthUid != null) {
            entityOverallFollowersRepo.decrementFollowers(id)
            entityWeeklyFollowersRepo.decrementFollowers(id)
        }
    }

    override fun findAll(): List<SHORTDTO> {
        return entityRepo.findAll()
    }

    override fun findByPartOfName(namePart: String): List<SHORTDTO> {
        return entityRepo.findByPartOfName(namePart)
    }
}