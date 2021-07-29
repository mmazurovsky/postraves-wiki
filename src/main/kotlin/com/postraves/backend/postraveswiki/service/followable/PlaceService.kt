package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.data.dto.reading.PlaceFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.PlaceShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.SceneDto
import com.postraves.backend.postraveswiki.data.dto.writing.PlaceWriteDto
import com.postraves.backend.postraveswiki.exception.UpdateException
import com.postraves.backend.postraveswiki.repo.followable.PlaceRepo
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepoAbstract
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.WeeklyBestQuickRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface PlaceService :
    BaseService<PlaceWriteDto, PlaceShortDto>,
    ByIdService<PlaceFullDto, PlaceShortDto>,
    RatingService<PlaceFullDto, PlaceShortDto>,
    FindByName<PlaceShortDto> {
        fun getAllScenes(): List<SceneDto>
        fun getScenesOfPlace(id: Long): List<SceneDto>
        fun updateScenesOfPlace(id: Long, incomingScenes: List<SceneDto>)
    }

@Service
class PlaceServiceImpl(
    val cityService: CityService,
    securityService: SecurityService,
    private val placeRepo: PlaceRepo,
    @Qualifier("placeCountryQuickRepoImpl")
    private val placeCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("placeWeeklyBestQuickRepoImpl")
    private val weeklyBestRepo: WeeklyBestQuickRepo,
    @Qualifier("placeWeeklyFollowersQuickRepoImpl")
    private val placeWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("placeOverallFollowersQuickRepoImpl")
    private val placeOverallFollowersQuickRepo: FollowersQuickRepo,
) : PlaceService,
    AbstractService<PlaceWriteDto, PlaceFullDto, PlaceShortDto, PlaceRepo>(
        cityService = cityService,
        securityService = securityService,
        entityRepo = placeRepo,
        entityCountryRepo = placeCountryRepo,
        weeklyBestRepo = weeklyBestRepo,
        entityOverallFollowersQuickRepo = placeOverallFollowersQuickRepo,
        entityWeeklyFollowersQuickRepo = placeWeeklyFollowersQuickRepo,
    ) {

    override fun checkCountryAndRemoveFromCountryQuickRepo(dto: PlaceFullDto) {
        val countryOfDtoToDelete = dto.city.country.name
            placeCountryRepo.removeOneIdFromSet(countryOfDtoToDelete, dto.id)
    }

    override fun checkCountryAndAddToCountryQuickRepo(dto: PlaceWriteDto, id: Long) {
        val cityDto = cityService.findByName(dto.cityName)
        placeCountryRepo.addOneIdToCountry(cityDto.country.name, id)
    }

    override fun checkCountryAndAddAndRemoveFromCountryQuickRepo(dto: PlaceWriteDto) {
        val newCountryName = cityService.findByName(dto.cityName).country.name
        val previousCountryName = placeRepo.findById(null, dto.id ?: throw UpdateException("Place", dto.name)).city.country.name
        if (newCountryName != previousCountryName) {
                placeCountryRepo.addOneIdToCountry(newCountryName, dto.id)
                placeCountryRepo.removeOneIdFromSet(previousCountryName, dto.id)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeShortDtoFromMap(map: Map<String, String>): PlaceShortDto {
        return PlaceShortDto.fromMap(map)
    }

    override fun getAllScenes(): List<SceneDto> {
        return placeRepo.getAllScenes()
    }

    override fun getScenesOfPlace(id: Long): List<SceneDto> {
        return placeRepo.getScenesOfPlace(id)
    }

    override fun updateScenesOfPlace(id: Long, incomingScenes: List<SceneDto>) {
        val placePersistedScenes = placeRepo.getScenesOfPlace(id).toSet()
        val scenesToAddToPlace = mutableSetOf<SceneDto>()
        val scenesToUpdate = mutableSetOf<SceneDto>()
        val sceneIdsInIncoming = mutableSetOf<Long>()
        incomingScenes.forEach {
            if (it.id == null) {
                scenesToAddToPlace.add(it)
            } else {
                sceneIdsInIncoming.add(it.id)
                if (!placePersistedScenes.contains(it)) {
                    scenesToUpdate.add(it)
                }
            }
        }
        val scenesToRemoveFromPlace = placePersistedScenes.filter {
            !sceneIdsInIncoming.contains(it.id)
        }.toSet()
        placeRepo.removeScenes(scenesToRemoveFromPlace)
        placeRepo.addScenesToPlace(id, scenesToAddToPlace)
        placeRepo.updateScenes(scenesToUpdate)
    }

    override fun enrichWithFollowersCalculationRequired(dto: PlaceShortDto): PlaceShortDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

    override fun enrichWithFollowersCalculationRequired(dto: PlaceFullDto): PlaceFullDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }
}