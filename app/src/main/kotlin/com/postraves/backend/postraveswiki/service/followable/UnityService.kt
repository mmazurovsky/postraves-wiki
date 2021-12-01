package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.UnityWriteDto
import com.postraves.backend.postraveswiki.data.enum.EntityType
import com.postraves.backend.postraveswiki.exception.UpdateException
import com.postraves.backend.postraveswiki.repo.followable.UnityRepo
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepoAbstract
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.WeeklyBestQuickRepo
import com.postraves.backend.postraveswiki.service.*
import com.postraves.backend.postraveswiki.service.RatingsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

interface UnityService :
    BaseService<UnityWriteDto, UnityShortDto>,
    ByIdService<UnityFullDto, UnityShortDto>,
    FollowableService<UnityFullDto, UnityShortDto>,
    RatingsService<UnityFullDto, UnityShortDto>,
    FindByName<UnityShortDto> {
    fun getArtistsOfUnity(id: Long): List<ArtistShortDto>
    fun updateArtistsOfUnity(id: Long, artists: Set<Long>)
}

@Service
class UnityServiceImpl(
    private val artistService: ArtistService,
    private val thisRepo: UnityRepo,
    @Qualifier("unityCountryQuickRepoImpl")
    private val thisCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("unityWeeklyBestQuickRepoImpl")
    private val weeklyBestRepo: WeeklyBestQuickRepo,
    @Qualifier("unityWeeklyFollowersQuickRepoImpl")
    private val thisWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("unityOverallFollowersQuickRepoImpl")
    private val thisOverallFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("unityRatingsServiceImpl")
    private val ratingsService: RatingsService<UnityFullDto, UnityShortDto>
) : UnityService,
    AbstractFollowableService<UnityWriteDto, UnityFullDto, UnityShortDto, UnityRepo>(
        entityRepo = thisRepo,
        entityOverallFollowersQuickRepo = thisOverallFollowersQuickRepo,
        entityWeeklyFollowersQuickRepo = thisWeeklyFollowersQuickRepo,
    ) {

    @Autowired
    @Lazy
    private lateinit var myUserProfileService: MyUserProfileService

    val thisString = EntityType.UNITY.nameString

    override fun checkLocationsAndRemoveFromLocationsQuickRepos(dto: UnityFullDto) {
        val countryOfDtoToDelete = dto.country?.name
        if (countryOfDtoToDelete != null) {
            thisCountryRepo.removeOneIdFromSet(countryOfDtoToDelete, dto.id)
        }
    }

    override fun checkLocationsAndAddToLocationsQuickRepos(dto: UnityWriteDto, id: Long) {
        if (dto.countryName != null) thisCountryRepo.addOneIdToCountry(dto.countryName, id)
    }

    override fun checkLocationsAndAddAndRemoveFromLocationsQuickRepos(dto: UnityWriteDto) {
        val previousCountryName =
            thisRepo.findById(null, dto.id ?: throw UpdateException(thisString, dto.name)).country?.name
        if (dto.countryName != previousCountryName) {
            if (dto.countryName != null) {
                thisCountryRepo.addOneIdToCountry(dto.countryName, dto.id)
            }
            if (previousCountryName != null) {
                thisCountryRepo.removeOneIdFromSet(previousCountryName, dto.id)
            }
        }
    }

    override fun getArtistsOfUnity(id: Long): List<ArtistShortDto> {
        val authUid = myUserProfileService.getMyUserId()
        val artistsOfUnityWithoutFollowers = thisRepo.getArtistsOfUnity(authUid, id)
        return artistsOfUnityWithoutFollowers
            .map { artistService.enrichWithFollowersCalculationRequired(it) }
            .sortedByDescending { it.overallFollowers }
            .toList()
    }

    override fun updateArtistsOfUnity(id: Long, artists: Set<Long>) {
        val persistedArtistsIds =
            thisRepo.getArtistsOfUnity(null, id)
                .map { it.id }
                .toSet()

        val artistsToRemoveFromUnity = persistedArtistsIds subtract artists
        val artistsToAddToUnity = artists subtract persistedArtistsIds

        thisRepo.removeArtistsFromUnity(id, artistsToRemoveFromUnity)
        thisRepo.addArtistsToUnity(id, artistsToAddToUnity)
    }

    override fun enrichWithFollowersCalculationRequired(dto: UnityShortDto): UnityShortDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

    override fun enrichWithFollowersCalculationRequired(dto: UnityFullDto): UnityFullDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

//    override fun findListByIds(ids: Set<Long>): List<UnityShortDto> {
//        return ratingsService.findListByIds(ids)
//    }

    override fun findOverallRatingInCountryForCity(cityName: String, maxQuantity: Int): List<UnityShortDto> {
        return ratingsService.findOverallRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findWeeklyRatingInCountryForCity(cityName: String, maxQuantity: Int): List<UnityShortDto> {
        return ratingsService.findWeeklyRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findBestOfTheWeekByCityInCountry(cityName: String): UnityShortDto? {
        return ratingsService.findBestOfTheWeekByCityInCountry(cityName)
    }

    override fun setBestOfTheWeekForAllCities() {
        ratingsService.setBestOfTheWeekForAllCities()
    }

    override fun removeBestOfTheWeekByCityInCountry(cityName: String) {
        ratingsService.removeBestOfTheWeekByCityInCountry(cityName)
    }
}