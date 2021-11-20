package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.data.converters.EventConverters
import com.postraves.backend.postraveswiki.data.converters.TimetableConverters
import com.postraves.backend.postraveswiki.data.dto.TicketPriceDto
import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.EventWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.TimetablePerformanceWriteDto
import com.postraves.backend.postraveswiki.data.enum.EntityType
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.SaveException
import com.postraves.backend.postraveswiki.repo.BaseRepo
import com.postraves.backend.postraveswiki.repo.ByIdRepo
import com.postraves.backend.postraveswiki.repo.FollowableRepo
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import jooq.tables.records.*
import jooq.tables.references.*
import org.jooq.*
import org.jooq.impl.DSL.lower
import org.jooq.impl.DSL.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface EventRepo :
    BaseRepo<EventWriteDto, EventShortDto>,
    ByIdRepo<EventFullDto, EventShortDto>,
    FollowableRepo<EventShortDto> {
    fun getRelevantEventsForArtist(userId: Long?, artistId: Long): List<EventShortDto>
    fun getRelevantEventsForPlace(userId: Long?, placeId: Long): List<EventShortDto>
    fun getRelevantEventsForUnity(userId: Long?, unityId: Long): List<EventShortDto>
    fun getEventsByCityAndTimeInterval(
        userId: Long?,
        cityName: String,
        startOfIntervalDateTime: OffsetDateTime,
        endOfIntervalDateTime: OffsetDateTime
    ): List<EventShortDto>
    fun getOrganizers(userId: Long?, id: Long): List<UnityShortDto>
    fun addOrganizers(id: Long, orgs: Set<Long>)
    fun removeOrganizers(id: Long, orgs: Set<Long>)
    fun getLineup(userId: Long?, id: Long): List<ArtistShortDto>
    fun getTimetableForEvent(userId: Long?, id: Long, isForAdmin: Boolean): List<TimetableForSceneDto>
    fun getTimetableItemsForEvent(eventId: Long): Set<TimetablePerformanceWriteDto>
    fun addTimetablePerformances(id: Long, performances: Set<TimetablePerformanceWriteDto>)
    fun updateTimetablePerformancesNotTouchingArtists(id: Long, performances: Set<TimetablePerformanceWriteDto>)
    fun removeTimetablePerformances(ids: Set<Long>)
}

@Repository
class EventRepoImpl(
    private val timetableConverters: TimetableConverters,
    private val eventConverters: EventConverters,
    private val dateTimeProvider: DateTimeProvider,
    private val artistRepo: ArtistRepo,
    private val unityRepo: UnityRepo
) :
    EventRepo,
    AbstractRepo<EventWriteDto, EventFullDto, EventShortDto, EventRecord>(
        table = EVENT,
        entityType = EntityType.EVENT.nameString
    ) {

    @Qualifier("getDSLContext")
    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    private val thisTable = EVENT
    private val thisString = EntityType.EVENT.nameString
    private val userFollowsTable = USER_FOLLOWS_EVENT

    override fun SelectJoinStep<Record>.joinLocation(): SelectOnConditionStep<Record> {
        return joinEventLocation()
    }

    override fun SelectJoinStep<Record>.joinUserFollow(userId: Long): SelectOnConditionStep<Record> {
        return joinEventUserFollow(userId)
    }

    override fun SelectJoinStep<Record>.joinOtherData(): SelectOnConditionStep<Record>? {
        return null
    }

    override fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record> {
        return this.where(thisTable.EVENT_ID.eq(id))
    }

    private fun saveTicketPrices(id: Long, ticketPrices: Collection<TicketPriceDto>) {
        ticketPrices.forEach {
            val ticketPriceRecord = dsl.newRecord(TICKET_PRICE)
            ticketPriceRecord.apply {
                it.transferDataToDbRecord(this)
                this.ticketPriceCreatedDateTime = dateTimeProvider.getNow()
                this.ticketPriceEventId = id
            }
            ticketPriceRecord.store()
        }
    }

    private fun removeTicketPricesOfEvent(id: Long) {
        dsl
            .delete(TICKET_PRICE)
            .where(TICKET_PRICE.TICKET_PRICE_EVENT_ID.eq(id))
            .execute()
    }

    private fun getTicketPrices(id: Long): List<TicketPriceRecord> {
        return dsl
            .selectFrom(TICKET_PRICE)
            .where(TICKET_PRICE.TICKET_PRICE_EVENT_ID.eq(id))
            .fetch()
            .toList()
    }

    // getting tickets from here might be not optimal
    override fun convertToShortDto(record: Record): EventShortDto {
        val isFollowed = record.into(userFollowsTable).userFollowsEventUserProfileId != null
        val isPlaceFollowed = record.into(USER_FOLLOWS_PLACE).userFollowsPlaceUserProfileId != null

        val eventRecord = record.into(thisTable)
        val ticketPrices = getTicketPrices(
            eventRecord.eventId ?: throw NotFoundException(
                thisString,
                "null id of event on trying to convert"
            )
        )

        return eventConverters.createShortDtoFromRecords(
            eventRecord = eventRecord,
            ticketPrices = ticketPrices,
            placeRecord = record.into(PLACE),
            cityRecord = record.into(CITY),
            countryRecord = record.into(COUNTRY),
            isEventFollowed = isFollowed,
            isPlaceFollowed = isPlaceFollowed,
        )
    }

    // getting tickets from here might be not optimal
    override fun convertToFullDto(record: Record): EventFullDto {
        val isFollowed = record.into(userFollowsTable).userFollowsEventUserProfileId != null
        val isPlaceFollowed = record.into(USER_FOLLOWS_PLACE).userFollowsPlaceUserProfileId != null

        val eventRecord = record.into(thisTable)
        val ticketPrices = getTicketPrices(
            eventRecord.eventId ?: throw NotFoundException(
                thisString,
                "null id of event on trying to convert"
            )
        )

        return eventConverters.createFullDtoFromRecords(
            eventRecord = eventRecord,
            ticketPrices = ticketPrices,
            placeRecord = record.into(PLACE),
            cityRecord = record.into(CITY),
            countryRecord = record.into(COUNTRY),
            isEventFollowed = isFollowed,
            isPlaceFollowed = isPlaceFollowed,
        )
    }

    override fun SelectWhereStep<Record>.whereIdIsInIds(ids: Set<Long>): SelectConditionStep<Record> {
        return this.where(thisTable.EVENT_ID.`in`(ids))
    }

    override fun SelectWhereStep<Record>.whereNameIsLike(namePart: String): SelectConditionStep<Record> {
        return this.where(lower(thisTable.EVENT_NAME).contains(namePart.lowercase()))
    }

    override fun prepareRecordBeforeSaving(record: EventRecord, dto: EventWriteDto) {
        eventConverters.transferDataFromDtoToRecord(dto, record)
        record.eventCreatedDateTime = dateTimeProvider.getNow()
        record.eventIsCancelled = false
    }

    override fun postSaveGetId(record: EventRecord): Long {
        return record.eventId ?: throw SaveException(thisString, record.eventName ?: "NULL")
    }

    override fun postSaveProcessing(id: Long, dto: EventWriteDto) {
        if (dto.ticketPrices != null && dto.ticketPrices.isNotEmpty()) saveTicketPrices(id, dto.ticketPrices)
        if (dto.organizers != null && dto.organizers.isNotEmpty()) addOrganizers(id, dto.organizers)
    }

    override fun postUpdateProcessing(dto: EventWriteDto) {
        removeTicketPricesOfEvent(dto.id ?: throw TODO())
        if (dto.ticketPrices != null && dto.ticketPrices.isNotEmpty()) saveTicketPrices(dto.id, dto.ticketPrices)
    }

    override fun preUpdateGetId(dto: EventWriteDto): Long {
        return dto.id ?: throw NotFoundException(thisString, dto.id.toString())
    }

    override fun prepareRecordBeforeUpdating(record: EventRecord, dto: EventWriteDto) {
        eventConverters.transferDataFromDtoToRecord(dto, record)
    }

    override fun getRelevantEventsForArtist(userId: Long?, artistId: Long): List<EventShortDto> {
        // todo too many joins here, better store event - artist relation in redis

        val nestedSelectUpcomingEventsOfArtist = select()
            .distinctOn(EVENT.EVENT_ID)
            .from(EVENT)
            .joinLocation()
            .leftOuterJoin(TIMETABLE_ITEM).on(TIMETABLE_ITEM.TIMETABLE_ITEM_EVENT_ID.eq(EVENT.EVENT_ID))
            .leftOuterJoin(TIMETABLE_ITEM_PERFORMING_GROUP)
            .on(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_PERFORMING_GROUP_TIMETABLE_ITEM_ID.eq(TIMETABLE_ITEM.TIMETABLE_ITEM_ID))
            .leftOuterJoin(ARTIST)
            .on(ARTIST.ARTIST_ID.eq(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_PERFORMING_GROUP_ARTIST_ID))
            .apply {
                if (userId != null) joinUserFollow(userId)
            }
            .where(ARTIST.ARTIST_ID.eq(artistId).and(EVENT.EVENT_END_DATE_TIME.gt(dateTimeProvider.getNow())))
            .asTable("nested")

        return dsl
            .selectFrom(
                nestedSelectUpcomingEventsOfArtist
            )
            .orderBy(nestedSelectUpcomingEventsOfArtist.field("event_start_date_time")!!.asc())
            .fetch()
            .map {
                this.convertToShortDto(it)
            }
            .toList()
    }

    override fun getRelevantEventsForPlace(userId: Long?, placeId: Long): List<EventShortDto> {
        return dsl
            .select()
            .from(EVENT)
            .joinLocation()
            .apply { if (userId != null) joinUserFollow(userId) }
            .where(PLACE.PLACE_ID.eq(placeId).and(EVENT.EVENT_END_DATE_TIME.gt(dateTimeProvider.getNow())))
            .orderBy(EVENT.EVENT_START_DATE_TIME.asc())
            .fetch()
            .map {
                this.convertToShortDto(it)
            }
            .toList()
    }

    override fun getRelevantEventsForUnity(userId: Long?, unityId: Long): List<EventShortDto> {
        return dsl
            .select()
            .from(EVENT)
            .joinLocation()
            .leftOuterJoin(UNITY_EVENT).on(UNITY_EVENT.UNITY_EVENT_EVENT_ID.eq(EVENT.EVENT_ID))
            .leftOuterJoin(UNITY).on(UNITY.UNITY_ID.eq(UNITY_EVENT.UNITY_EVENT_UNITY_ID))
            .apply { if (userId != null) joinUserFollow(userId) }
            .where(UNITY.UNITY_ID.eq(unityId).and(EVENT.EVENT_END_DATE_TIME.gt(dateTimeProvider.getNow())))
            .orderBy(EVENT.EVENT_START_DATE_TIME.asc())
            .fetch()
            .map {
                this.convertToShortDto(it)
            }
            .toList()
    }


    override fun getEventsByCityAndTimeInterval(
        userId: Long?,
        cityName: String,
        startOfIntervalDateTime: OffsetDateTime,
        endOfIntervalDateTime: OffsetDateTime
    ): List<EventShortDto> {
        val events = dsl
            .select()
            .from(EVENT)
            .joinLocation()
            .apply { if (userId != null) joinUserFollow(userId) }
            // offset datetime now is with what offset
            .where(
                PLACE.PLACE_CITY_NAME.eq(cityName)
                    .and(EVENT.EVENT_END_DATE_TIME.between(startOfIntervalDateTime, endOfIntervalDateTime))
            )
            .orderBy(EVENT.EVENT_START_DATE_TIME.asc())
            .fetch()
            .map {
                this.convertToShortDto(it)
            }
            .toList()

        return events
    }

    override fun getOrganizers(userId: Long?, id: Long): List<UnityShortDto> {
        return dsl
            .select()
            .from(UNITY_EVENT)
            .leftOuterJoin(UNITY).on(UNITY.UNITY_ID.eq(UNITY_EVENT.UNITY_EVENT_UNITY_ID))
            .leftOuterJoin(COUNTRY).on(COUNTRY.COUNTRY_NAME.eq(UNITY.UNITY_COUNTRY_NAME))
            .leftOuterJoin(USER_FOLLOWS_UNITY)
            .on(
                USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_UNITY_ID.eq(UNITY.UNITY_ID),
                USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_USER_PROFILE_ID.eq(userId)
            )
            .where(UNITY_EVENT.UNITY_EVENT_EVENT_ID.eq(id))
            .fetch()
            .map {
                unityRepo.convertToShortDto(it)
            }
            .toList()
    }

    override fun addOrganizers(id: Long, orgs: Set<Long>) {
        orgs.forEach {
            dsl
                .newRecord(UNITY_EVENT)
                .apply {
                    unityEventUnityId = it
                    unityEventEventId = id
                }
                .store()
        }
    }

    override fun removeOrganizers(id: Long, orgs: Set<Long>) {
        orgs.forEach {
            dsl
                .delete(UNITY_EVENT)
                .where(UNITY_EVENT.UNITY_EVENT_UNITY_ID.eq(it), UNITY_EVENT.UNITY_EVENT_EVENT_ID.eq(id))
                .execute()
        }
    }

    override fun getLineup(userId: Long?, id: Long): List<ArtistShortDto> {
        return dsl
            .select()
            .distinctOn(ARTIST.ARTIST_ID)
            .from(TIMETABLE_ITEM)
            .leftOuterJoin(TIMETABLE_ITEM_PERFORMING_GROUP)
            .on(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_PERFORMING_GROUP_TIMETABLE_ITEM_ID.eq(TIMETABLE_ITEM.TIMETABLE_ITEM_ID))
            .leftOuterJoin(ARTIST)
            .on(ARTIST.ARTIST_ID.eq(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_PERFORMING_GROUP_ARTIST_ID))
            .leftOuterJoin(COUNTRY).on(COUNTRY.COUNTRY_NAME.eq(ARTIST.ARTIST_COUNTRY_NAME))
            .leftOuterJoin(USER_FOLLOWS_ARTIST)
            .on(
                ARTIST.ARTIST_ID.eq(USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_ARTIST_ID),
                USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_USER_PROFILE_ID.eq(userId)
            )
            .where(TIMETABLE_ITEM.TIMETABLE_ITEM_EVENT_ID.eq(id))
            .fetch()
            .map {
                artistRepo.convertToShortDto(it)
            }
            .toList()
    }

    override fun getTimetableForEvent(userId: Long?, id: Long, isForAdmin: Boolean): List<TimetableForSceneDto> {

        val cityOfEvent = dsl
            .select()
            .from(EVENT)
            .leftOuterJoin(PLACE).on(PLACE.PLACE_ID.eq(EVENT.EVENT_PLACE_ID))
            .leftOuterJoin(CITY).on(CITY.CITY_NAME.eq(PLACE.PLACE_CITY_NAME))
            .where(EVENT.EVENT_ID.eq(id))
            .fetchInto(CITY)

        val timeOffsetOfCity = cityOfEvent[0].cityTimeOffset ?: TODO()

        val timetableItems = dsl
            .select()
            .from(TIMETABLE_ITEM)
            .leftOuterJoin(SCENE).on(SCENE.SCENE_ID.eq(TIMETABLE_ITEM.TIMETABLE_ITEM_SCENE_ID))
            .where(TIMETABLE_ITEM.TIMETABLE_ITEM_EVENT_ID.eq(id))
            // todo needs testing
            .orderBy(
                SCENE.SCENE_PRIORITY.desc().nullsFirst(),
                TIMETABLE_ITEM.TIMETABLE_ITEM_STARTING_DATE_TIME.asc().nullsLast()
            )
            .fetch()

        val mapOfScenePerformances: MutableMap<SceneRecord?, MutableList<Pair<TimetableItemRecord, List<Triple<ArtistRecord, CountryRecord, Boolean>>>>> =
            mutableMapOf()

        timetableItems.forEach { record ->
            val timetableItemRecord = record.into(TIMETABLE_ITEM)
            val sceneOfTimetableItem = record.into(SCENE)

            if (!isForAdmin) {
                if (
                    sceneOfTimetableItem.sceneId == null
                    ||
                    timetableItemRecord.timetableItemStartingDateTime == null
                    ||
                    timetableItemRecord.timetableItemEndingDateTime == null
                ) {
                    return@forEach
                }
            }

            val artistsForTimetableItem = dsl
                .select()
                .from(TIMETABLE_ITEM_PERFORMING_GROUP)
                .leftOuterJoin(ARTIST)
                .on(ARTIST.ARTIST_ID.eq(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_PERFORMING_GROUP_ARTIST_ID))
                .leftOuterJoin(COUNTRY).on(COUNTRY.COUNTRY_NAME.eq(ARTIST.ARTIST_COUNTRY_NAME))
                .leftOuterJoin(USER_FOLLOWS_ARTIST)
                .on(
                    ARTIST.ARTIST_ID.eq(USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_ARTIST_ID),
                    USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_USER_PROFILE_ID.eq(userId)
                )
                .where(
                    TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_PERFORMING_GROUP_TIMETABLE_ITEM_ID.eq(
                        timetableItemRecord.timetableItemId
                    )
                )
                .fetch()

            // todo equality of records is questionable
            if (mapOfScenePerformances.containsKey(sceneOfTimetableItem)) {
                mapOfScenePerformances[sceneOfTimetableItem]?.add(timetableItemRecord to artistsForTimetableItem.map {
                    Triple(
                        it.into(ARTIST),
                        it.into(COUNTRY),
                        it.into(USER_FOLLOWS_ARTIST).userFollowsArtistUserProfileId != null
                    )
                }.toList())
            } else {
                mapOfScenePerformances[sceneOfTimetableItem] =
                    mutableListOf(timetableItemRecord to artistsForTimetableItem.map {
                        Triple(
                            it.into(ARTIST),
                            it.into(COUNTRY),
                            it.into(USER_FOLLOWS_ARTIST).userFollowsArtistUserProfileId != null
                        )
                    }.toList())
            }
        }

        return mapOfScenePerformances.map {
            timetableConverters.createTimetableForSceneDto(it.key, it.value, timeOffsetOfCity)
        }.toList()
    }

    override fun getTimetableItemsForEvent(eventId: Long): Set<TimetablePerformanceWriteDto> {
        return dsl
            .select()
            .from(TIMETABLE_ITEM)
            .leftOuterJoin(TIMETABLE_ITEM_PERFORMING_GROUP)
            .on(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_PERFORMING_GROUP_TIMETABLE_ITEM_ID.eq(TIMETABLE_ITEM.TIMETABLE_ITEM_ID))
            .where(TIMETABLE_ITEM.TIMETABLE_ITEM_EVENT_ID.eq(eventId))
            .fetch()
            .map {
                val timetableItem = it.into(TIMETABLE_ITEM)
                val artistIds = dsl
                    .selectFrom(TIMETABLE_ITEM_PERFORMING_GROUP)
                    .where(
                        TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_PERFORMING_GROUP_TIMETABLE_ITEM_ID.eq(
                            timetableItem.timetableItemId
                        )
                    )
                    .fetch()
                    .map { performingGroup ->
                        performingGroup.into(TIMETABLE_ITEM_PERFORMING_GROUP)
                            .timetableItemPerformingGroupArtistId
                    }
                    .toSet()
                timetableConverters.createTimetablePerformanceWriteDto(timetableItem, artistIds)
            }
            .toSet()
    }

    override fun addTimetablePerformances(id: Long, performances: Set<TimetablePerformanceWriteDto>) {
        performances.forEach { performance ->
            val timetableItem = dsl
                .newRecord(TIMETABLE_ITEM)
            timetableItem
                .apply {
                    timetableConverters.transferDataFromDtoToRecord(performance, this)
                    this.timetableItemEventId = id
                    this.timetableItemCreatedDateTime = dateTimeProvider.getNow()
                }
                .store()

            performance.artistIds.forEach {
                dsl
                    .newRecord(TIMETABLE_ITEM_PERFORMING_GROUP)
                    .apply {
                        this.timetableItemPerformingGroupArtistId = it
                        this.timetableItemPerformingGroupTimetableItemId = timetableItem.timetableItemId
                    }
                    .store()
            }
        }
    }

    override fun updateTimetablePerformancesNotTouchingArtists(
        id: Long,
        performances: Set<TimetablePerformanceWriteDto>
    ) {
        performances.forEach { performance ->
            val timetableItem = dsl
                .selectFrom(TIMETABLE_ITEM)
                .where(TIMETABLE_ITEM.TIMETABLE_ITEM_ID.eq(performance.id))
                .fetchOne() ?: throw TODO()
            timetableItem
                .apply {
                    timetableConverters.transferDataFromDtoToRecord(performance, this)
                }
                .update()
        }
    }

    override fun removeTimetablePerformances(ids: Set<Long>) {
        ids.forEach {
            dsl
                .delete(TIMETABLE_ITEM)
                .where(TIMETABLE_ITEM.TIMETABLE_ITEM_ID.eq(it))
                .execute()
        }
    }
}