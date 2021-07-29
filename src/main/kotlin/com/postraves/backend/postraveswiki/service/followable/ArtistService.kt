package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.exception.UpdateException
import com.postraves.backend.postraveswiki.repo.followable.ArtistRepo
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepoAbstract
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.WeeklyBestQuickRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface ArtistService :
    BaseService<ArtistWriteDto, ArtistShortDto>,
    ByIdService<ArtistFullDto, ArtistShortDto>,
    RatingService<ArtistFullDto, ArtistShortDto>,
    FindByName<ArtistShortDto>

@Service
class ArtistServiceImpl(
    cityService: CityService,
    securityService: SecurityService,
    private val artistRepo: ArtistRepo,
    @Qualifier("artistCountryQuickRepoImpl")
    private val artistCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("artistWeeklyBestQuickRepoImpl")
    private val weeklyBestRepo: WeeklyBestQuickRepo,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val artistWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("artistOverallFollowersQuickRepoImpl")
    private val artistOverallFollowersQuickRepo: FollowersQuickRepo,
) : ArtistService,
    AbstractService<ArtistWriteDto, ArtistFullDto, ArtistShortDto, ArtistRepo>(
        cityService = cityService,
        securityService = securityService,
        entityRepo = artistRepo,
        entityCountryRepo = artistCountryRepo,
        weeklyBestRepo = weeklyBestRepo,
        entityOverallFollowersQuickRepo = artistOverallFollowersQuickRepo,
        entityWeeklyFollowersQuickRepo = artistWeeklyFollowersQuickRepo,
    ) {

    override fun checkCountryAndRemoveFromCountryQuickRepo(dto: ArtistFullDto) {
        val countryOfDtoToDelete = dto.country?.name
        if (countryOfDtoToDelete != null) {
            artistCountryRepo.removeOneIdFromSet(countryOfDtoToDelete, dto.id)
        }
    }

    override fun checkCountryAndAddToCountryQuickRepo(dto: ArtistWriteDto, id: Long) {
        if (dto.countryName != null) artistCountryRepo.addOneIdToCountry(dto.countryName, id)
    }

    override fun checkCountryAndAddAndRemoveFromCountryQuickRepo(dto: ArtistWriteDto) {
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

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeShortDtoFromMap(map: Map<String, String>): ArtistShortDto {
        return ArtistShortDto.fromMap(map)
    }

    override fun enrichWithFollowersCalculationRequired(dto: ArtistShortDto): ArtistShortDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

    override fun enrichWithFollowersCalculationRequired(dto: ArtistFullDto): ArtistFullDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }
}