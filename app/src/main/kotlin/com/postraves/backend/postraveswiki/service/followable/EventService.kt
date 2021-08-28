package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.EventWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.TimetablePerformanceWriteDto
import com.postraves.backend.postraveswiki.repo.followable.EventRepo
import com.postraves.backend.postraveswiki.repo.followable.PlaceRepo
import com.postraves.backend.postraveswiki.repo.quick.EntityCountryQuickRepoAbstract
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.*
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDate

interface EventService :
    BaseService<EventWriteDto, EventShortDto>,
    ByIdService<EventFullDto, EventShortDto>,
    FollowableService<EventFullDto, EventShortDto>,
    FindByName<EventShortDto> {
    fun getRelevantEventsForArtist(artistId: Long): List<EventShortDto>
    fun getRelevantEventsForPlace(placeId: Long): List<EventShortDto>
    fun getRelevantEventsForUnity(unityId: Long): List<EventShortDto>
    fun getEventsByDate(cityName: String): List<EventsByDateDto>
    fun getEventsByRating(cityName: String): List<EventShortDto>
    fun getOrganizers(id: Long): List<UnityShortDto>
    fun updateOrganizers(id: Long, orgs: Set<Long>)
    fun getLineup(id: Long): List<ArtistShortDto>
    fun getTimetableForEvent(id: Long): List<TimetableForSceneDto>
    fun updateTimetableForEvent(id: Long, performances: Set<TimetablePerformanceWriteDto>)
}

@Service
class EventServiceImpl(
    private val dateTimeProvider: DateTimeProvider,
    private val unityService: UnityService,
    private val artistService: ArtistService,
    private val eventRepo: EventRepo,
    private val placeRepo: PlaceRepo,
    @Qualifier("eventCountryQuickRepoImpl")
    private val eventCountryRepo: EntityCountryQuickRepoAbstract,
    @Qualifier("eventWeeklyFollowersQuickRepoImpl")
    private val eventWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("eventOverallFollowersQuickRepoImpl")
    private val eventOverallFollowersQuickRepo: FollowersQuickRepo,
) : EventService,
    AbstractFollowableService<EventWriteDto, EventFullDto, EventShortDto, EventRepo>(
        entityRepo = eventRepo,
        entityOverallFollowersQuickRepo = eventOverallFollowersQuickRepo,
        entityWeeklyFollowersQuickRepo = eventWeeklyFollowersQuickRepo,
    ) {

    @Autowired
    @Lazy
    private lateinit var myUserProfileService: MyUserProfileService

    override fun checkLocationsAndRemoveFromLocationsQuickRepos(dto: EventFullDto) {
        val countryOfDtoToDelete = dto.place.city.country.name
        eventCountryRepo.removeOneIdFromSet(countryOfDtoToDelete, dto.id)
    }

    override fun checkLocationsAndAddToLocationsQuickRepos(dto: EventWriteDto, id: Long) {
        val place = placeRepo.findById(null, dto.placeId)
        val countryName = place.city.country.name
        eventCountryRepo.addOneIdToCountry(countryName, id)
    }

    override fun checkLocationsAndAddAndRemoveFromLocationsQuickRepos(dto: EventWriteDto) {
        val preservedEntity = eventRepo.findById(null, dto.id ?: throw TODO())
        val previousCountryName = preservedEntity.place.city.country.name

        val newPlace = placeRepo.findById(null, dto.placeId)
        val newCountryName = newPlace.city.country.name

        if (newCountryName != previousCountryName) {
            eventCountryRepo.removeOneIdFromSet(previousCountryName, dto.id)
            eventCountryRepo.addOneIdToCountry(newCountryName, dto.id)
        }
    }

    override fun getRelevantEventsForArtist(artistId: Long): List<EventShortDto> {
        val func = { authUid: String?, entityId: Long -> eventRepo.getRelevantEventsForArtist(authUid, entityId) }
        return getRelevantEventsForEntity(func, artistId)
    }

    override fun getRelevantEventsForPlace(placeId: Long): List<EventShortDto> {
        val func = { authUid: String?, entityId: Long -> eventRepo.getRelevantEventsForPlace(authUid, entityId) }
        return getRelevantEventsForEntity(func, placeId)
    }

    override fun getRelevantEventsForUnity(unityId: Long): List<EventShortDto> {
        val func = { authUid: String?, entityId: Long -> eventRepo.getRelevantEventsForUnity(authUid, entityId) }
        return getRelevantEventsForEntity(func, unityId)
    }

    private fun getRelevantEventsForEntity(func: (authUid: String?, entityId: Long) -> List<EventShortDto>, entityId: Long): List<EventShortDto> {
        val authUid = myUserProfileService.getMyAuthUidOnlyIfUserProfileExists()
        val eventsWithoutFollowers = func(authUid, entityId)
        val eventsWithFollowers = eventsWithoutFollowers.map { this.enrichWithFollowersCalculationRequired(it) }.toList()
        return eventsWithFollowers
    }

    override fun getEventsByDate(cityName: String): List<EventsByDateDto> {
        val authUid = myUserProfileService.getMyAuthUidOnlyIfUserProfileExists()
        val startIntervalDateTime = dateTimeProvider.getNow()
        val endIntervalDateTime = startIntervalDateTime.plusDays(31)
        val eventsWithoutFollowers = eventRepo.getEventsByCityAndTimeInterval(authUid, cityName, startIntervalDateTime, endIntervalDateTime)

        val eventsWithFollowersSorted = this.enrichListWithFollowersAndSortByOverallFollowers(eventsWithoutFollowers)

        val eventsByDate = mutableMapOf<LocalDate, EventsByDateDto>()
        eventsWithFollowersSorted.forEach {
            val localDateTimeOfEvent = it.startDateTime.toLocalDate()
            if (eventsByDate.containsKey(localDateTimeOfEvent)) {
                eventsByDate[localDateTimeOfEvent]?.events?.add(it) ?: throw TODO()
            } else {
                eventsByDate[localDateTimeOfEvent] = EventsByDateDto(localDateTimeOfEvent, mutableListOf(it))
            }
        }

        val eventsByDateWithFollowersSorted = eventsByDate.values.sortedBy { it.date }.toList()
        return eventsByDateWithFollowersSorted
    }

    override fun getEventsByRating(cityName: String): List<EventShortDto> {
        val authUid = myUserProfileService.getMyAuthUidOnlyIfUserProfileExists()
        val startIntervalDateTime = dateTimeProvider.getNow()
        val endIntervalDateTime = startIntervalDateTime.plusDays(31)
        val eventsWithoutFollowers = eventRepo.getEventsByCityAndTimeInterval(authUid, cityName, startIntervalDateTime, endIntervalDateTime)
        val eventsWithFollowersSorted = this.enrichListWithFollowersAndSortByOverallFollowers(eventsWithoutFollowers)
        return eventsWithFollowersSorted
    }

    override fun getOrganizers(id: Long): List<UnityShortDto> {
        val authUid = myUserProfileService.getMyAuthUidOnlyIfUserProfileExists()
        val orgsWithoutFollowers = eventRepo.getOrganizers(authUid, id)
        return unityService.enrichListWithFollowersAndSortByOverallFollowers(orgsWithoutFollowers)
    }

    override fun updateOrganizers(id: Long, orgs: Set<Long>) {
        val persistetOrgs = eventRepo.getOrganizers(null, id)
        val persistedOrgsIds = persistetOrgs.map { it.id }.toSet()

        val orgsToDelete = persistedOrgsIds subtract orgs
        val orgsToAdd = orgs subtract persistedOrgsIds

        eventRepo.removeOrganizers(id, orgsToDelete)
        eventRepo.addOrganizers(id, orgsToAdd)
    }

    override fun getLineup(id: Long): List<ArtistShortDto> {
        val authUid = myUserProfileService.getMyAuthUidOnlyIfUserProfileExists()
        val lineupWithoutFollowers = eventRepo.getLineup(authUid, id)
        return artistService.enrichListWithFollowersAndSortByOverallFollowers(lineupWithoutFollowers)
    }

    override fun getTimetableForEvent(id: Long): List<TimetableForSceneDto> {
        val authUid = myUserProfileService.getMyAuthUidOnlyIfUserProfileExists()
        val timetableWithArtistsNotEnrichedWithFollowers = eventRepo.getTimetableForEvent(authUid, id)
        return enrichTimetableArtistsWithFollowers(timetableWithArtistsNotEnrichedWithFollowers)
    }

    private fun enrichTimetableArtistsWithFollowers(timetable: List<TimetableForSceneDto>): List<TimetableForSceneDto> {
        return timetable.map { timetableForScene ->
            timetableForScene.copy(
                performances = timetableForScene.performances.map {
                    it.copy(
                        artists = artistService.enrichListWithFollowersAndSortByOverallFollowers(it.artists)
                    )
                }
                    .sortedBy { it.startingDateTime }
                    .toList()
            )
        }.toList()
    }

    override fun updateTimetableForEvent(id: Long, performances: Set<TimetablePerformanceWriteDto>) {
        val persistedPerformances = eventRepo.getTimetableItemsForEvent(id)
        val persistedPerformanceIds = persistedPerformances.map { it.id ?: throw TODO() }.toSet()
        val performancesToSave = mutableSetOf<TimetablePerformanceWriteDto>()
        val performanceIdsToRemove = mutableSetOf<Long>()
        val performancesToUpdateNotTouchingArtists = mutableSetOf<TimetablePerformanceWriteDto>()
        val incomingPerformanceIds = mutableSetOf<Long>()
        performances.forEach { incomingPerformance ->
            if (!persistedPerformances.contains(incomingPerformance)) {
                if (incomingPerformance.id == null) {
                    performancesToSave.add(incomingPerformance)
                } else {
                    incomingPerformanceIds.add(incomingPerformance.id)
                    val outdatedPersistedPerformance =
                        persistedPerformances.find { it.id == incomingPerformance.id } ?: throw TODO()
                    if (outdatedPersistedPerformance.artistIds != incomingPerformance.artistIds) {
                        outdatedPersistedPerformance.id?.let { performanceIdsToRemove.add(it) }
                        performancesToSave.add(incomingPerformance)
                    } else {
                        performancesToUpdateNotTouchingArtists.add(incomingPerformance)
                    }
                }
            }
        }

        performanceIdsToRemove.addAll(persistedPerformanceIds subtract incomingPerformanceIds)

        eventRepo.removeTimetablePerformances(performanceIdsToRemove)
        eventRepo.updateTimetablePerformancesNotTouchingArtists(id, performancesToUpdateNotTouchingArtists)
        eventRepo.addTimetablePerformances(id, performancesToSave)
    }

    override fun enrichWithFollowersCalculationRequired(dto: EventShortDto): EventShortDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }

    override fun enrichWithFollowersCalculationRequired(dto: EventFullDto): EventFullDto {
        return super.enrichWithFollowersCalculationRequired(dto)
    }
}