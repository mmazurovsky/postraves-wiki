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
    fun getEventsByDate(cityName: String): List<EventShortDto>
    fun getEventsByRating(cityName: String): List<EventShortDto>
    fun getOrganizers(id: Long): List<UnityShortDto>
    fun updateOrganizers(id: Long, orgs: Set<Long>)
    fun getLineup(id: Long): List<ArtistShortDto>
    fun getTimetableForEvent(id: Long, isForAdmin: Boolean): List<TimetableForSceneDto>
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
    @Qualifier("eventImageUploader")
    private val eventImageUploader: ImageUploaderAbstract,
    ) : EventService,
    AbstractFollowableService<EventWriteDto, EventFullDto, EventShortDto, EventRepo>(
        entityRepo = eventRepo,
        entityOverallFollowersQuickRepo = eventOverallFollowersQuickRepo,
        entityWeeklyFollowersQuickRepo = eventWeeklyFollowersQuickRepo,
        imageUploader = eventImageUploader
    ) {

    @Autowired
    @Lazy
    private lateinit var myUserProfileService: MyUserProfileService

    companion object {
        private const val timeFrameRelevant: Long = 90
    }

    override fun preProcessBeforeSaving(dto: EventWriteDto) {
        if (dto.id != null) throw TODO()
    }

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
        val func = { userId: Long?, entityId: Long -> eventRepo.getRelevantEventsForArtist(userId, entityId) }
        return getRelevantEventsForEntity(func, artistId)
    }

    override fun getRelevantEventsForPlace(placeId: Long): List<EventShortDto> {
        val func = { userId: Long?, entityId: Long -> eventRepo.getRelevantEventsForPlace(userId, entityId) }
        return getRelevantEventsForEntity(func, placeId)
    }

    override fun getRelevantEventsForUnity(unityId: Long): List<EventShortDto> {
        val func = { userId: Long?, entityId: Long -> eventRepo.getRelevantEventsForUnity(userId, entityId) }
        return getRelevantEventsForEntity(func, unityId)
    }

    private fun getRelevantEventsForEntity(
        func: (userId: Long?, entityId: Long) -> List<EventShortDto>,
        entityId: Long
    ): List<EventShortDto> {
        val userId = myUserProfileService.getMyUserId()
        val eventsWithoutFollowers = func(userId, entityId)
        val eventsWithFollowers =
            eventsWithoutFollowers.map { this.enrichWithFollowersCalculationRequired(it) }.toList()
        return eventsWithFollowers
    }

    override fun getEventsByDate(cityName: String): List<EventShortDto> {
        val authUid = myUserProfileService.getMyUserId()
        val startIntervalDateTime = dateTimeProvider.getNow()
        val endIntervalDateTime = startIntervalDateTime.plusDays(timeFrameRelevant)
        val eventsWithoutFollowers =
            eventRepo.getEventsByCityAndTimeInterval(authUid, cityName, startIntervalDateTime, endIntervalDateTime)

        val eventsWithFollowersSorted = this.enrichListWithFollowers(eventsWithoutFollowers)

        return eventsWithFollowersSorted
    }

    override fun getEventsByRating(cityName: String): List<EventShortDto> {
        val authUid = myUserProfileService.getMyUserId()
        val startIntervalDateTime = dateTimeProvider.getNow()
        val endIntervalDateTime = startIntervalDateTime.plusDays(timeFrameRelevant)
        val eventsWithoutFollowers =
            eventRepo.getEventsByCityAndTimeInterval(authUid, cityName, startIntervalDateTime, endIntervalDateTime)
        val eventsWithFollowersSorted = this.enrichListWithFollowersAndSortByOverallFollowers(eventsWithoutFollowers)
        return eventsWithFollowersSorted
    }

    override fun getOrganizers(id: Long): List<UnityShortDto> {
        val authUid = myUserProfileService.getMyUserId()
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
        val authUid = myUserProfileService.getMyUserId()
        val lineupWithoutFollowers = eventRepo.getLineup(authUid, id)
        return artistService.enrichListWithFollowersAndSortByOverallFollowers(lineupWithoutFollowers)
    }

    override fun getTimetableForEvent(id: Long, isForAdmin: Boolean): List<TimetableForSceneDto> {
        val authUid = myUserProfileService.getMyUserId()
        val timetableWithArtistsNotEnrichedWithFollowers = eventRepo.getTimetableForEvent(authUid, id, isForAdmin)
        return enrichTimetableArtistsWithFollowersAndSort(timetableWithArtistsNotEnrichedWithFollowers)
    }

    private fun enrichTimetableArtistsWithFollowersAndSort(timetable: List<TimetableForSceneDto>): List<TimetableForSceneDto> {
        return timetable.map { timetableForScene ->
            timetableForScene.copy(
                performances = timetableForScene.performances.map {
                    it.copy(
                        artists = artistService.enrichListWithFollowersAndSortByOverallFollowers(it.artists)
                    )
                }
                    .sortedWith(nullsFirst(compareBy { it.startingDateTime }))
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
                    // once that are completely new
                    performancesToSave.add(incomingPerformance)
                } else {
                    incomingPerformanceIds.add(incomingPerformance.id)
                    val outdatedPersistedPerformance =
                        persistedPerformances.find { it.id == incomingPerformance.id } ?: throw TODO()
                    if (outdatedPersistedPerformance.artistIds != incomingPerformance.artistIds) {
                        // once that are old and have modified data that is performing artists get resaved with new id
                        outdatedPersistedPerformance.id?.let { performanceIdsToRemove.add(it) }
                        performancesToSave.add(incomingPerformance)
                    } else {
                        // once that are old and have modified data that is not performing artists
                        performancesToUpdateNotTouchingArtists.add(incomingPerformance)
                    }
                }
            } else {
                // ones that didn't change
                incomingPerformanceIds.add(incomingPerformance.id!!)
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