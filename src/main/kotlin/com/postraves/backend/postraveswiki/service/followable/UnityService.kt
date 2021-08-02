package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UnityWriteDto
import com.postraves.backend.postraveswiki.data.enum.EntityType
import com.postraves.backend.postraveswiki.exception.UpdateException
import com.postraves.backend.postraveswiki.repo.followable.UnityRepo
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepoAbstract
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.WeeklyBestQuickRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface UnityService :
    BaseService<UnityWriteDto, UnityShortDto>,
    ByIdService<UnityFullDto, UnityShortDto>,
    RatingService<UnityFullDto, UnityShortDto>,
    FindByName<UnityShortDto> {
    fun getArtistsOfUnity(id: Long): List<ArtistShortDto>
    fun updateArtistsOfUnity(id: Long, artists: Set<Long>)
}

@Service
class UnityServiceImpl(
    cityService: CityService,
    securityService: SecurityService,
    private val thisRepo: UnityRepo,
    @Qualifier("unityCountryQuickRepoImpl")
    private val thisCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("unityWeeklyBestQuickRepoImpl")
    private val weeklyBestRepo: WeeklyBestQuickRepo,
    @Qualifier("unityWeeklyFollowersQuickRepoImpl")
    private val thisWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("unityOverallFollowersQuickRepoImpl")
    private val thisOverallFollowersQuickRepo: FollowersQuickRepo,
) : UnityService,
    AbstractService<UnityWriteDto, UnityFullDto, UnityShortDto, UnityRepo>(
        cityService = cityService,
        securityService = securityService,
        entityRepo = thisRepo,
        entityCountryRepo = thisCountryRepo,
        weeklyBestRepo = weeklyBestRepo,
        entityOverallFollowersQuickRepo = thisOverallFollowersQuickRepo,
        entityWeeklyFollowersQuickRepo = thisWeeklyFollowersQuickRepo,
    ) {

    val thisString = EntityType.UNITY.nameString

    override fun checkCountryAndRemoveFromCountryQuickRepo(dto: UnityFullDto) {
        val countryOfDtoToDelete = dto.country?.name
        if (countryOfDtoToDelete != null) {
            thisCountryRepo.removeOneIdFromSet(countryOfDtoToDelete, dto.id)
        }
    }

    override fun checkCountryAndAddToCountryQuickRepo(dto: UnityWriteDto, id: Long) {
        if (dto.countryName != null) thisCountryRepo.addOneIdToCountry(dto.countryName, id)
    }

    override fun checkCountryAndAddAndRemoveFromCountryQuickRepo(dto: UnityWriteDto) {
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

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeShortDtoFromMap(map: Map<String, String>): UnityShortDto {
        return UnityShortDto.fromMap(map)
    }

    override fun getArtistsOfUnity(id: Long): List<ArtistShortDto> {
        // todo abstract somehow
        val user = myUserProfileService.findMyProfile()
        return if (user.first == null)
            thisRepo.getArtistsOfUnity(null, id)
        else
            thisRepo.getArtistsOfUnity(user.second, id)
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
}