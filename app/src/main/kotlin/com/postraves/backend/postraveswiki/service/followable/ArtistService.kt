package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.EventShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.exception.UpdateException
import com.postraves.backend.postraveswiki.repo.followable.ArtistRepo
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepoAbstract
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.*
import com.postraves.backend.postraveswiki.service.RatingsService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface ArtistService :
    BaseService<ArtistWriteDto, ArtistShortDto>,
    ByIdService<ArtistFullDto, ArtistShortDto>,
    FollowableService<ArtistFullDto, ArtistShortDto>,
    RatingsService<ArtistFullDto, ArtistShortDto>,
    FindByName<ArtistShortDto>

@Service
class ArtistServiceImpl(
    private val artistRepo: ArtistRepo,
    @Qualifier("artistCountryQuickRepoImpl")
    private val artistCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val artistWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("artistOverallFollowersQuickRepoImpl")
    private val artistOverallFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("artistRatingsServiceImpl")
    private val ratingsService: RatingsService<ArtistFullDto, ArtistShortDto>
) : ArtistService,
    AbstractFollowableService<ArtistWriteDto, ArtistFullDto, ArtistShortDto, ArtistRepo>(
        entityRepo = artistRepo,
        entityOverallFollowersQuickRepo = artistOverallFollowersQuickRepo,
        entityWeeklyFollowersQuickRepo = artistWeeklyFollowersQuickRepo,
    ) {

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
        val previousCountryName = artistRepo.findById(null, dto.id ?: throw UpdateException("Artist", dto.name)).country?.name
        if (dto.countryName != previousCountryName) {
            if (dto.countryName != null) {
                artistCountryRepo.addOneIdToCountry(dto.countryName, dto.id)
            }
            if (previousCountryName != null) {
                artistCountryRepo.removeOneIdFromSet(previousCountryName, dto.id)
            }
        }
    }

    override fun enrichWithFollowersCalculationRequired(dto: ArtistShortDto): ArtistShortDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

    override fun enrichWithFollowersCalculationRequired(dto: ArtistFullDto): ArtistFullDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

//    override fun findListByIds(ids: Set<Long>): List<ArtistShortDto> {
//        return ratingsService.findListByIds(ids)
//    }

    override fun findOverallRatingInCountryForCity(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        return ratingsService.findOverallRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findWeeklyRatingInCountryForCity(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        return ratingsService.findWeeklyRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findBestOfTheWeekByCityInCountry(cityName: String): ArtistShortDto? {
        return ratingsService.findBestOfTheWeekByCityInCountry(cityName)
    }

    override fun setBestOfTheWeekForAllCities() {
        ratingsService.setBestOfTheWeekForAllCities()
    }
}