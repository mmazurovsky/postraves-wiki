package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.repo.ArtistRepo
import com.postraves.backend.postraveswiki.repo.QuickEntityCountryRepoAbstract
import com.postraves.backend.postraveswiki.repo.QuickFollowersRepo
import com.postraves.backend.postraveswiki.repo.WeeklyBestRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
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
    private val artistCountryRepo: QuickEntityCountryRepoAbstract,
    @Qualifier("artistWeeklyBestRepoImpl")
    private val weeklyBestRepo: WeeklyBestRepo,
    @Qualifier("artistWeeklyQuickFollowersRepoImpl")
    private val artistWeeklyFollowersDeltaRepo: QuickFollowersRepo,
    @Qualifier("artistOverallQuickFollowersRepoImpl")
    private val artistOverallFollowersImplRepo: QuickFollowersRepo,
) : ArtistService,
    AbstractService<ArtistWriteDto, ArtistFullDto, ArtistShortDto, ArtistRepo>(
        cityService = cityService,
        securityService = securityService,
        entityRepo = artistRepo,
        entityCountryRepo = artistCountryRepo,
        weeklyBestRepo = weeklyBestRepo,
        entityOverallFollowersRepo = artistOverallFollowersImplRepo,
        entityWeeklyFollowersRepo = artistWeeklyFollowersDeltaRepo,
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
        val previousCountryName = artistRepo.findById(dto.id ?: throw TODO()).country?.name
        if (dto.countryName != previousCountryName) {
            if (dto.countryName != null) {
                artistCountryRepo.addOneIdToCountry(dto.countryName, dto.id)
            }
            if (previousCountryName != null) {
                artistCountryRepo.removeOneIdFromSet(previousCountryName, dto.id)
            }
        }
    }

    override fun decodeShortDtoFromJson(encoded: JsonElement): ArtistShortDto {
        return Json.decodeFromJsonElement(encoded)
    }

    override fun enrichWithFollowersCalculationRequired(dto: ArtistShortDto): ArtistShortDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

    override fun enrichWithFollowersCalculationRequired(dto: ArtistFullDto): ArtistFullDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }
}