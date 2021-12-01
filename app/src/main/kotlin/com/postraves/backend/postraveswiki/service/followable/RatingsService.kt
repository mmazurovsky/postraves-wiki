package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.converters.ArtistConverters
import com.postraves.backend.postraveswiki.data.converters.PlaceConverters
import com.postraves.backend.postraveswiki.data.converters.UnityConverters
import com.postraves.backend.postraveswiki.data.dto.ConvertableToMap
import com.postraves.backend.postraveswiki.data.dto.FollowableDto
import com.postraves.backend.postraveswiki.data.dto.FollowableFullDto
import com.postraves.backend.postraveswiki.data.dto.FollowableShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.repo.ByIdRepo
import com.postraves.backend.postraveswiki.repo.followable.ArtistRepo
import com.postraves.backend.postraveswiki.repo.followable.PlaceRepo
import com.postraves.backend.postraveswiki.repo.followable.UnityRepo
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepoAbstract
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.WeeklyBestQuickRepo
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.RatingsService
import kotlinx.serialization.ExperimentalSerializationApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.min

abstract class RatingsServiceAbstractImpl<FULLDTO : FollowableFullDto<FULLDTO>, SHORTDTOCONVERTABLE, REPO>(
    private val entityCountryRepo: EntityCountryQuickRepoAbstract,
    private val weeklyBestRepo: WeeklyBestQuickRepo,
    private val entityWeeklyFollowersQuickRepo: FollowersQuickRepo,
    private val entityOverallFollowersQuickRepo: FollowersQuickRepo,
    private val entityRepo: REPO,
    private val cityService: CityService,
    private val decodeShortDtoFromMap: (map: Map<String, String>) -> SHORTDTOCONVERTABLE
) : RatingsService<FULLDTO, SHORTDTOCONVERTABLE>
        where SHORTDTOCONVERTABLE : FollowableShortDto<SHORTDTOCONVERTABLE>,
              SHORTDTOCONVERTABLE : ConvertableToMap,
              REPO : ByIdRepo<FULLDTO, SHORTDTOCONVERTABLE> {

    @Autowired
    @Lazy
    private lateinit var myUserProfileService: MyUserProfileService

    private fun findListByIds(ids: Set<Long>): List<SHORTDTOCONVERTABLE> {
        val authUid = myUserProfileService.getMyUserId()
        return entityRepo.findListByIds(authUid, ids)
    }

    private fun <T : FollowableDto<T>> enrichWithFollowersWithoutCalculation(
        dto: T,
        overallFollowers: Int,
        weeklyFollowers: Int
    ): T {
        return dto.copyWithFollowersEnriched(overallFollowers, weeklyFollowers)
    }

    private fun findAbstractRatingForCityByCountry(
        mainFollowersQuickRepo: FollowersQuickRepo,
        cityName: String,
        maxQuantity: Int,
    ): Pair<List<SHORTDTOCONVERTABLE>, Map<Long, Int>> {
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

    override fun setBestOfTheWeekForAllCities() {
        val allCities = cityService.findAll()
        allCities.forEach {
            val topEntityInCountryOfCityList = findWeeklyRatingInCountryForCity(it.name, 1)
            if (topEntityInCountryOfCityList.size == 1) {
                val topEntityInCountryOfCity = topEntityInCountryOfCityList[0]
                weeklyBestRepo.setWeeklyBestInCountry(it.country.name, topEntityInCountryOfCity.toMap())
            } else {
                // todo i don't know what to set, maybe nothing
                logger.debug("Can't get top entity to set it as weekly best")
//                throw WeeklyBestSettingException("Can't get top entity to set it as weekly best")
            }
        }
    }

    override fun removeBestOfTheWeekByCityInCountry(cityName: String) {
        val countryName = cityService.findByName(cityName).country.name
        weeklyBestRepo.removeWeeklyBestInCountry(countryName)
    }

    // this returns isFollow that was set initially, not possible new value
    override fun findBestOfTheWeekByCityInCountry(cityName: String): SHORTDTOCONVERTABLE? {
        val countryName = cityService.findByName(cityName).country.name
        val bestEntityAsMap = weeklyBestRepo.getWeeklyBestInCountry(countryName)
        return if (bestEntityAsMap != null) decodeShortDtoFromMap(bestEntityAsMap) else null
    }

    override fun findOverallRatingInCountryForCity(cityName: String, maxQuantity: Int): List<SHORTDTOCONVERTABLE> {

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

    override fun findWeeklyRatingInCountryForCity(cityName: String, maxQuantity: Int): List<SHORTDTOCONVERTABLE> {
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
}

@OptIn(ExperimentalSerializationApi::class)
@Service
class ArtistRatingsServiceImpl(
    @Qualifier("artistCountryQuickRepoImpl")
    artistCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("artistWeeklyBestQuickRepoImpl")
    weeklyBestRepo: WeeklyBestQuickRepo,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    artistWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("artistOverallFollowersQuickRepoImpl")
    artistOverallFollowersQuickRepo: FollowersQuickRepo,
    artistRepo: ArtistRepo,
    cityService: CityService,
    artistConverters: ArtistConverters,
) : RatingsServiceAbstractImpl<ArtistFullDto, ArtistShortDto, ArtistRepo>(
    entityCountryRepo = artistCountryRepo,
    weeklyBestRepo = weeklyBestRepo,
    entityWeeklyFollowersQuickRepo = artistWeeklyFollowersQuickRepo,
    entityOverallFollowersQuickRepo = artistOverallFollowersQuickRepo,
    entityRepo = artistRepo,
    cityService = cityService,
    decodeShortDtoFromMap = { map: Map<String, String> -> artistConverters.createShortDtoFromMap(map) }
)

@OptIn(ExperimentalSerializationApi::class)
@Service
class PlaceRatingsServiceImpl(
    @Qualifier("placeCountryQuickRepoImpl")
    placeCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("placeWeeklyBestQuickRepoImpl")
    weeklyBestRepo: WeeklyBestQuickRepo,
    @Qualifier("placeWeeklyFollowersQuickRepoImpl")
    placeWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("placeOverallFollowersQuickRepoImpl")
    placeOverallFollowersQuickRepo: FollowersQuickRepo,
    placeRepo: PlaceRepo,
    cityService: CityService,
    placeConverters: PlaceConverters,
) : RatingsServiceAbstractImpl<PlaceFullDto, PlaceShortDto, PlaceRepo>(
    entityCountryRepo = placeCountryRepo,
    weeklyBestRepo = weeklyBestRepo,
    entityWeeklyFollowersQuickRepo = placeWeeklyFollowersQuickRepo,
    entityOverallFollowersQuickRepo = placeOverallFollowersQuickRepo,
    entityRepo = placeRepo,
    cityService = cityService,
    decodeShortDtoFromMap = { map: Map<String, String> -> placeConverters.createShortDtoFromMap(map) }
)

@OptIn(ExperimentalSerializationApi::class)
@Service
class UnityRatingsServiceImpl(
    @Qualifier("unityCountryQuickRepoImpl")
    unityCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("unityWeeklyBestQuickRepoImpl")
    weeklyBestRepo: WeeklyBestQuickRepo,
    @Qualifier("unityWeeklyFollowersQuickRepoImpl")
    unityWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("unityOverallFollowersQuickRepoImpl")
    unityOverallFollowersQuickRepo: FollowersQuickRepo,
    unityRepo: UnityRepo,
    cityService: CityService,
    unityConverters: UnityConverters,
) : RatingsServiceAbstractImpl<UnityFullDto, UnityShortDto, UnityRepo>(
    entityCountryRepo = unityCountryRepo,
    weeklyBestRepo = weeklyBestRepo,
    entityWeeklyFollowersQuickRepo = unityWeeklyFollowersQuickRepo,
    entityOverallFollowersQuickRepo = unityOverallFollowersQuickRepo,
    entityRepo = unityRepo,
    cityService = cityService,
    decodeShortDtoFromMap = { map: Map<String, String> -> unityConverters.createShortDtoFromMap(map) }
)
