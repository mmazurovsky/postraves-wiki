package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.exception.UpdateException
import com.postraves.backend.postraveswiki.repo.followable.ArtistRepo
import com.postraves.backend.postraveswiki.repo.followable.MyUserProfileRepo
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepoAbstract
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

interface ArtistService :
    BaseService<ArtistWriteDto, ArtistShortDto>,
    ByIdService<ArtistFullDto, ArtistShortDto>,
    FollowableService<ArtistFullDto, ArtistShortDto>,
    RatingsService<ArtistFullDto, ArtistShortDto>,
    FindByName<ArtistShortDto> {
    fun getUnitiesOfArtist(id: Long): List<UnityShortDto>
}

@Service
class ArtistServiceImpl(
    @Lazy
    private val unityService: UnityService,
    @Lazy
    private val myUserProfileService: MyUserProfileService,
    private val thisRepo: ArtistRepo,
    @Qualifier("artistCountryQuickRepoImpl")
    private val artistCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val artistWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("artistOverallFollowersQuickRepoImpl")
    private val artistOverallFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("artistImageUploader")
    private val artistImageUploader: ImageUploaderAbstract,
    @Qualifier("artistRatingsServiceImpl")
    private val ratingsService: RatingsService<ArtistFullDto, ArtistShortDto>
) : ArtistService,
    AbstractFollowableService<ArtistWriteDto, ArtistFullDto, ArtistShortDto, ArtistRepo>(
        entityRepo = thisRepo,
        entityOverallFollowersQuickRepo = artistOverallFollowersQuickRepo,
        entityWeeklyFollowersQuickRepo = artistWeeklyFollowersQuickRepo,
        imageUploader = artistImageUploader,
    ) {

//    @Autowired
//    @Lazy
//    private lateinit var myUserProfileService: MyUserProfileService

    override fun checkLocationsAndRemoveFromLocationsQuickRepos(dto: ArtistFullDto) {
        val countryOfDtoToDelete = dto.country?.name
        if (countryOfDtoToDelete != null) {
            artistCountryRepo.removeOneIdFromSet(countryOfDtoToDelete, dto.id)
        }
    }

    override fun checkLocationsAndAddToLocationsQuickRepos(dto: ArtistWriteDto, id: Long) {
        if (dto.countryName != null) artistCountryRepo.addOneIdToCountry(dto.countryName, id)
    }

    override fun checkLocationsAndAddAndRemoveFromLocationsQuickRepos(dto: ArtistWriteDto) {
        val previousCountryName =
            thisRepo.findById(null, dto.id ?: throw UpdateException("Artist", dto.name)).country?.name
        if (dto.countryName != previousCountryName) {
            if (dto.countryName != null) {
                artistCountryRepo.addOneIdToCountry(dto.countryName, dto.id)
            }
            if (previousCountryName != null) {
                artistCountryRepo.removeOneIdFromSet(previousCountryName, dto.id)
            }
        }
    }

    override fun getUnitiesOfArtist(id: Long): List<UnityShortDto> {
        val authUid = myUserProfileService.getMyUserId()
        val unitiesOfArtistWithoutFollowers = thisRepo.getUnitiesOfArtist(authUid, id)
        return unitiesOfArtistWithoutFollowers
            .map { unityService.enrichWithFollowersCalculationRequired(it) }
            .sortedByDescending { it.overallFollowers }
            .toList()
    }

    override fun enrichWithFollowersCalculationRequired(dto: ArtistShortDto): ArtistShortDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

    override fun enrichWithFollowersCalculationRequired(dto: ArtistFullDto): ArtistFullDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

    override fun findOverallRatingInCountryForCity(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        return ratingsService.findOverallRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findWeeklyRatingInCountryForCity(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        return ratingsService.findWeeklyRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findBestOfTheWeekByCityInCountry(cityName: String): ArtistShortDto? {
        val artistOfTheWeekWithOutdatedFollowersAndWithoutIsFollowed = ratingsService.findBestOfTheWeekByCityInCountry(cityName)
        return if (artistOfTheWeekWithOutdatedFollowersAndWithoutIsFollowed == null)
            null
        else {
            val artistIsFollowed = myUserProfileService.checkArtistIsFollowed(artistOfTheWeekWithOutdatedFollowersAndWithoutIsFollowed.id)
            val artistOfTheWeekWithOutdatedFollowersAndWithIsFollowed = artistOfTheWeekWithOutdatedFollowersAndWithoutIsFollowed.copy(isFollowed = artistIsFollowed)
            artistOfTheWeekWithOutdatedFollowersAndWithIsFollowed
        }
    }

    override fun setBestOfTheWeekForAllCities() {
        ratingsService.setBestOfTheWeekForAllCities()
    }

    override fun removeBestOfTheWeekByCityInCountry(cityName: String) {
        ratingsService.removeBestOfTheWeekByCityInCountry(cityName)
    }
}