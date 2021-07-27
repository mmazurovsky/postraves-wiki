package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseFullDtoWithIdAndRating
import com.postraves.backend.postraveswiki.data.dto.BaseRatingDtoWithId
import com.postraves.backend.postraveswiki.data.dto.BaseShortDtoWithIdAndRating
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.WeeklyBestSettingException
import com.postraves.backend.postraveswiki.repo.*
import com.postraves.backend.postraveswiki.security.SecurityService
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
    private val entityWeeklyFollowersQuickRepo: FollowersQuickRepo,
    private val entityOverallFollowersQuickRepo: FollowersQuickRepo,
    private val entityRepo: REPO,
) : BaseService<WRITEDTO, SHORTDTO>, FindByName<SHORTDTO>, ByIdService<FULLDTO, SHORTDTO>,
    RatingService<FULLDTO, SHORTDTO>
        where REPO : BaseRepo<WRITEDTO, SHORTDTO>,
              REPO : ByIdRepo<FULLDTO, SHORTDTO>,
              REPO : FindByNameRepo<SHORTDTO> {

    @Autowired
    private lateinit var myUserProfileService: MyUserProfileService

    private fun findByIdDependingOnUser(id: Long): FULLDTO {
        val user = myUserProfileService.findMyProfile()
        return if (user == null)
            entityRepo.findById(null, id)
        else
            entityRepo.findById(securityService.userAuthUid, id)
    }

    override fun findListByIds(ids: Set<Long>): List<SHORTDTO> {
        return entityRepo.findListByIds(ids)
    }

    override fun findById(id: Long): FULLDTO {
        val foundArtist = findByIdDependingOnUser(id)
        return enrichWithFollowersCalculationRequired(foundArtist)
    }

    private fun calculateFollowers(id: Long): Pair<Int, Int> {
        val overallFollowers = entityOverallFollowersQuickRepo.getFollowers(id)
        val weeklyFollowers = entityWeeklyFollowersQuickRepo.getFollowers(id)
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
        val dtoToDelete = entityRepo.findById(null, id)

        checkCountryAndRemoveFromCountryQuickRepo(dtoToDelete)

        // deleting form quick repos ratings
        entityOverallFollowersQuickRepo.removeId(id)
        entityWeeklyFollowersQuickRepo.removeId(id)

        entityRepo.deleteById(id)
    }

    abstract fun checkCountryAndRemoveFromCountryQuickRepo(dto: FULLDTO)

    override fun save(dto: WRITEDTO): SHORTDTO {
        val saved = entityRepo.save(dto)
        checkCountryAndAddToCountryQuickRepo(dto, saved.id)
        entityOverallFollowersQuickRepo.setInitialFollowers(saved.id)
        entityWeeklyFollowersQuickRepo.setInitialFollowers(saved.id)
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
        mainFollowersQuickRepo: FollowersQuickRepo,
        cityName: String,
        maxQuantity: Int,
    ): Pair<List<SHORTDTO>, Map<Long, Int>> {
        val countryName = cityService.findByName(cityName).country.name
        val entitiesFromTheCountry = entityCountryRepo.getAllIdsByCountry(countryName)
        val topEntityIdsAndScores = mainFollowersQuickRepo.findTop(-1)
        val topEntityIdsAndScoresFromTheCountry =
            topEntityIdsAndScores
                .filterKeys { entitiesFromTheCountry.contains(it) }
                .toMap()
        val topEntityFromTheCountryIds = topEntityIdsAndScoresFromTheCountry.keys
        val topEntityFromTheCountryIdsCropped = topEntityFromTheCountryIds.toList().subList(
            0,
            min(topEntityFromTheCountryIds.size, maxQuantity)
        ).toSet()
        val topEntityFromTheCountryDtos = findListByIds(topEntityFromTheCountryIdsCropped)
        Collections.sort(
            topEntityFromTheCountryDtos,
            Comparator.comparing { topEntityFromTheCountryIds.indexOf(it.id) })
        return topEntityFromTheCountryDtos to topEntityIdsAndScoresFromTheCountry
    }

    override fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO> {

        val resultFromAbstract =
            findAbstractRatingForCityByCountry(entityOverallFollowersQuickRepo, cityName, maxQuantity)
        val topEntityListWithoutFollowers = resultFromAbstract.first
        val topEntityIdsAndScoresFromTheCountry = resultFromAbstract.second
        val topEntitiesEnrichedWithFollowers = topEntityListWithoutFollowers.map {
            enrichWithFollowersWithoutCalculation(
                it,
                overallFollowers = topEntityIdsAndScoresFromTheCountry[it.id]
                    ?: throw NotFoundException("Overall followers in city $cityName of entity", it.id.toString()),
                weeklyFollowers = entityWeeklyFollowersQuickRepo.getFollowers(it.id)
            )
        }.toList()
        return topEntitiesEnrichedWithFollowers
    }

    override fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO> {
        val resultFromAbstract =
            findAbstractRatingForCityByCountry(entityWeeklyFollowersQuickRepo, cityName, maxQuantity)
        val topEntityListWithoutFollowers = resultFromAbstract.first
        val topEntityIdsAndScoresFromTheCountry = resultFromAbstract.second
        val topEntitiesEnrichedWithFollowers = topEntityListWithoutFollowers.map {
            enrichWithFollowersWithoutCalculation(
                it,
                overallFollowers = entityOverallFollowersQuickRepo.getFollowers(it.id),
                weeklyFollowers = topEntityIdsAndScoresFromTheCountry[it.id]
                    ?: throw NotFoundException("Weekly followers in city $cityName of entity", it.id.toString()),
            )
        }.toList()
        return topEntitiesEnrichedWithFollowers
    }

    override fun findBestOfTheWeekByCityInCountry(cityName: String): SHORTDTO {
        val countryName = cityService.findByName(cityName).country.name
        val bestEntityAsMap = weeklyBestRepo.getWeeklyBestInCountry(countryName)
        val bestEntity = decodeShortDtoFromMap(bestEntityAsMap)
        return bestEntity
    }

    abstract fun decodeShortDtoFromMap(map: Map<String, String>): SHORTDTO

    override fun setBestOfTheWeekForAllCities() {
        val allCities = cityService.findAll()
        allCities.forEach {
            val topEntityInCountryOfCityList = findWeeklyRatingForCityByCountry(it.name, 1)
            if (topEntityInCountryOfCityList.size == 1) {
                val topEntityInCountryOfCity = topEntityInCountryOfCityList[0]
                weeklyBestRepo.setWeeklyBestInCountry(it.country.name, topEntityInCountryOfCity.toMap())
            } else throw WeeklyBestSettingException("Can't get top entity to set it as weekly best")
        }
    }

    override fun incrementFollowers(id: Long) {
        if (securityService.userAuthUid != null) {
            entityOverallFollowersQuickRepo.incrementFollowers(id)
            entityWeeklyFollowersQuickRepo.incrementFollowers(id)
        }
    }

    override fun decrementFollowers(id: Long) {
        if (securityService.userAuthUid != null) {
            entityOverallFollowersQuickRepo.decrementFollowers(id)
            entityWeeklyFollowersQuickRepo.decrementFollowers(id)
        }
    }

    override fun findAll(): List<SHORTDTO> {
        return entityRepo.findAll()
    }

    override fun findByPartOfName(namePart: String): List<SHORTDTO> {
        return entityRepo.findByPartOfName(namePart)
    }
}