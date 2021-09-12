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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface EventRepo :
    BaseRepo<EventWriteDto, EventShortDto>,
    ByIdRepo<EventFullDto, EventShortDto>,
    FollowableRepo<EventShortDto> {
    fun getRelevantEventsForArtist(authUid: String?, artistId: Long): List<EventShortDto>
    fun getRelevantEventsForPlace(authUid: String?, placeId: Long): List<EventShortDto>
    fun getRelevantEventsForUnity(authUid: String?, unityId: Long): List<EventShortDto>
    fun getEventsByCityAndTimeInterval(authUid: String?, cityName: String, startOfIntervalDateTime: OffsetDateTime, endOfIntervalDateTime: OffsetDateTime): List<EventShortDto>
    fun getOrganizers(authUid: String?, id: Long): List<UnityShortDto>
    fun addOrganizers(id: Long, orgs: Set<Long>)
    fun removeOrganizers(id: Long, orgs: Set<Long>)
    fun getLineup(authUid: String?, id: Long): List<ArtistShortDto>
    fun getTimetableForEvent(authUid: String?, id: Long): List<TimetableForSceneDto>
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

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    val thisTable = EVENT
    val thisString = EntityType.EVENT.nameString
    val userFollowsTable = USER_FOLLOWS_EVENT

    override fun SelectJoinStep<Record>.joinLocation(): SelectOnConditionStep<Record> {
        return this
            .leftOuterJoin(PLACE).on(thisTable.PLACE_ID.eq(PLACE.ID))
            .leftOuterJoin(CITY).on(PLACE.CITY_NAME.eq(CITY.NAME))
            .leftOuterJoin(COUNTRY).on(CITY.COUNTRY_NAME.eq(COUNTRY.NAME))
    }

    override fun SelectJoinStep<Record>.joinUserFollow(authUid: String): SelectOnConditionStep<Record> {
        return this
            .leftOuterJoin(userFollowsTable)
            .on(thisTable.ID.eq(userFollowsTable.EVENT_ID), userFollowsTable.USER_PROFILE_UID.eq(authUid))
            .leftOuterJoin(USER_FOLLOWS_PLACE)
            .on(PLACE.ID.eq(USER_FOLLOWS_PLACE.PLACE_ID), USER_FOLLOWS_PLACE.USER_PROFILE_UID.eq(authUid))
    }

    override fun SelectJoinStep<Record>.joinOtherData(): SelectOnConditionStep<Record>? {
        return null
    }

    private fun SelectJoinStep<Record>.joinUserFollowArtist(authUid: String): SelectOnConditionStep<Record> {
        return this.leftOuterJoin(USER_FOLLOWS_ARTIST)
            .on(ARTIST.ID.eq(USER_FOLLOWS_ARTIST.ARTIST_ID), USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(authUid))
    }

    override fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record> {
        return this.where(thisTable.ID.eq(id))
    }

    private fun saveTicketPrices(id: Long, ticketPrices: Collection<TicketPriceDto>) {
        ticketPrices.forEach {
            val ticketPriceRecord = dsl.newRecord(TICKET_PRICE)
            ticketPriceRecord.apply {
                it.transferDataToDbRecord(this)
                this.createdDateTime = dateTimeProvider.getNow()
                this.eventId = id
            }
            ticketPriceRecord.store()
        }
    }

    private fun removeTicketPricesOfEvent(id: Long) {
        dsl
            .delete(TICKET_PRICE)
            .where(TICKET_PRICE.EVENT_ID.eq(id))
            .execute()
    }

    private fun getTicketPrices(id: Long): List<TicketPriceRecord> {
        return dsl
            .selectFrom(TICKET_PRICE)
            .where(TICKET_PRICE.EVENT_ID.eq(id))
            .fetch()
            .toList()
    }

    // getting tickets from here might be not optimal
    override fun convertToShortDto(record: Record): EventShortDto {
        val isFollowed = record.into(userFollowsTable).userProfileUid != null
        val isPlaceFollowed = record.into(USER_FOLLOWS_PLACE).userProfileUid != null

        val eventRecord = record.into(thisTable)
        val ticketPrices = getTicketPrices(
            eventRecord.id ?: throw NotFoundException(
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
        val isFollowed = record.into(userFollowsTable).userProfileUid != null
        val isPlaceFollowed = record.into(USER_FOLLOWS_PLACE).userProfileUid != null

        val eventRecord = record.into(thisTable)
        val ticketPrices = getTicketPrices(
            eventRecord.id ?: throw NotFoundException(
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
        return this.where(thisTable.ID.`in`(ids))
    }

    override fun SelectWhereStep<Record>.whereNameIsLike(namePart: String): SelectConditionStep<Record> {
        return this.where(lower(thisTable.NAME).contains(namePart.lowercase()))
    }

    override fun prepareRecordBeforeSaving(record: EventRecord, dto: EventWriteDto) {
        dto.transferDataToDbRecord(record)
        record.createdDateTime = dateTimeProvider.getNow()
        record.isCancelled = false
    }

    override fun postSaveGetId(record: EventRecord): Long {
        return record.id ?: throw SaveException(thisString, record.name ?: "NULL")
    }

    override fun postSaveProcessing(id: Long, dto: EventWriteDto) {
        if (dto.ticketPrices.isNotEmpty()) saveTicketPrices(id, dto.ticketPrices)
        if (dto.organizers.isNotEmpty()) addOrganizers(id, dto.organizers)
    }

    override fun postUpdateProcessing(dto: EventWriteDto) {
        removeTicketPricesOfEvent(dto.id ?: throw TODO())
        if (dto.ticketPrices.isNotEmpty()) saveTicketPrices(dto.id, dto.ticketPrices)
    }

    override fun preUpdateGetId(dto: EventWriteDto): Long {
        return dto.id ?: throw NotFoundException(thisString, dto.id.toString())
    }

    override fun prepareRecordBeforeUpdating(record: EventRecord, dto: EventWriteDto) {
        dto.transferDataToDbRecord(record)
    }

    override fun getRelevantEventsForArtist(authUid: String?, artistId: Long): List<EventShortDto> {
        // todo too many joins here, better store event - artist relation in redis
        return dsl
            .select()
            .from(EVENT)
            .joinLocation()
            .leftOuterJoin(TIMETABLE_ITEM).on(TIMETABLE_ITEM.EVENT_ID.eq(EVENT.ID))
            .leftOuterJoin(TIMETABLE_ITEM_PERFORMING_GROUP).on(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_ID.eq(TIMETABLE_ITEM.ID))
            .leftOuterJoin(ARTIST).on(ARTIST.ID.eq(TIMETABLE_ITEM_PERFORMING_GROUP.ARTIST_ID))
            .apply {if (authUid != null) joinUserFollow(authUid)}
            .where(ARTIST.ID.eq(artistId).and(EVENT.END_DATE_TIME.gt(dateTimeProvider.getNow())))
            .orderBy(EVENT.START_DATE_TIME.asc())
            .fetch()
            .map {
                this.convertToShortDto(it)
            }
            .toList()
    }

    override fun getRelevantEventsForPlace(authUid: String?, placeId: Long): List<EventShortDto> {
        return dsl
            .select()
            .from(EVENT)
            .joinLocation()
            .apply {if (authUid != null) joinUserFollow(authUid)}
            .where(PLACE.ID.eq(placeId).and(EVENT.END_DATE_TIME.gt(dateTimeProvider.getNow())))
            .orderBy(EVENT.START_DATE_TIME.asc())
            .fetch()
            .map {
                this.convertToShortDto(it)
            }
            .toList()
    }

    override fun getRelevantEventsForUnity(authUid: String?, unityId: Long): List<EventShortDto> {
        return dsl
            .select()
            .from(EVENT)
            .joinLocation()
            .leftOuterJoin(UNITY_EVENT).on(UNITY_EVENT.EVENT_ID.eq(EVENT.ID))
            .leftOuterJoin(UNITY).on(UNITY.ID.eq(UNITY_EVENT.UNITY_ID))
            .apply {if (authUid != null) joinUserFollow(authUid)}
            .where(UNITY.ID.eq(unityId).and(EVENT.END_DATE_TIME.gt(dateTimeProvider.getNow())))
            .orderBy(EVENT.START_DATE_TIME.asc())
            .fetch()
            .map {
                this.convertToShortDto(it)
            }
            .toList()
    }


    override fun getEventsByCityAndTimeInterval(authUid: String?, cityName: String, startOfIntervalDateTime: OffsetDateTime, endOfIntervalDateTime: OffsetDateTime): List<EventShortDto> {
        val events = dsl
            .select()
            .from(EVENT)
            .joinLocation()
            .apply {if (authUid != null) joinUserFollow(authUid)}
                // offset datetime now is with what offset
            .where(PLACE.CITY_NAME.eq(cityName).and(EVENT.END_DATE_TIME.between(startOfIntervalDateTime, endOfIntervalDateTime)))
            .orderBy(EVENT.START_DATE_TIME.asc())
            .fetch()
            .map {
                this.convertToShortDto(it)
            }
            .toList()

        return events
    }

    override fun getOrganizers(authUid: String?, id: Long): List<UnityShortDto> {
        return dsl
            .select()
            .from(UNITY_EVENT)
            .leftOuterJoin(UNITY).on(UNITY.ID.eq(UNITY_EVENT.UNITY_ID))
            .leftOuterJoin(COUNTRY).on(COUNTRY.NAME.eq(UNITY.COUNTRY_NAME))
            .leftOuterJoin(USER_FOLLOWS_UNITY).on(USER_FOLLOWS_UNITY.UNITY_ID.eq(UNITY.ID), USER_FOLLOWS_UNITY.USER_PROFILE_UID.eq(authUid))
            .where(UNITY_EVENT.EVENT_ID.eq(id))
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
                    unityId = it
                    eventId = id
                }
                .store()
        }
    }

    override fun removeOrganizers(id: Long, orgs: Set<Long>) {
        orgs.forEach {
            dsl
                .delete(UNITY_EVENT)
                .where(UNITY_EVENT.UNITY_ID.eq(it), UNITY_EVENT.EVENT_ID.eq(id))
                .execute()
        }
    }

    override fun getLineup(authUid: String?, id: Long): List<ArtistShortDto> {
        return dsl
            .select()
            .from(TIMETABLE_ITEM)
            .leftOuterJoin(TIMETABLE_ITEM_PERFORMING_GROUP).on(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_ID.eq(TIMETABLE_ITEM.ID))
            .leftOuterJoin(ARTIST).on(ARTIST.ID.eq(TIMETABLE_ITEM_PERFORMING_GROUP.ARTIST_ID))
            .leftOuterJoin(COUNTRY).on(COUNTRY.NAME.eq(ARTIST.COUNTRY_NAME))
            .leftOuterJoin(USER_FOLLOWS_ARTIST)
            .on(ARTIST.ID.eq(USER_FOLLOWS_ARTIST.ARTIST_ID), USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(authUid))
            .where(TIMETABLE_ITEM.EVENT_ID.eq(id))
            .fetch()
            .map {
                artistRepo.convertToShortDto(it)
            }
            .toList()
    }

    override fun getTimetableForEvent(authUid: String?, id: Long): List<TimetableForSceneDto> {
        val timetableItems = dsl
            .select()
            .from(TIMETABLE_ITEM)
            .leftOuterJoin(SCENE).on(SCENE.ID.eq(TIMETABLE_ITEM.SCENE_ID))
            .where(TIMETABLE_ITEM.EVENT_ID.eq(id))
                // todo needs testing
            .orderBy(SCENE.PRIORITY.desc().nullsLast(), TIMETABLE_ITEM.STARTING_DATE_TIME.asc().nullsLast())
            .fetch()

        val mapOfScenePerformances: MutableMap<SceneRecord, MutableList<Pair<TimetableItemRecord, List<Triple<ArtistRecord, CountryRecord, Boolean>>>>> =
            mutableMapOf()

        timetableItems.forEach { record ->
            val timetableItemRecord = record.into(TIMETABLE_ITEM)
            val sceneOfTimetableItem = record.into(SCENE)

            if (
                sceneOfTimetableItem.id == null
                ||
                timetableItemRecord.startingDateTime == null
                ||
                timetableItemRecord.endingDateTime == null
            ) {
                return@forEach
            }

            val artistsForTimetableItem = dsl
                .select()
                .from(TIMETABLE_ITEM_PERFORMING_GROUP)
                .leftOuterJoin(ARTIST).on(ARTIST.ID.eq(TIMETABLE_ITEM_PERFORMING_GROUP.ARTIST_ID))
                .leftOuterJoin(COUNTRY).on(COUNTRY.NAME.eq(ARTIST.COUNTRY_NAME))
                .leftOuterJoin(USER_FOLLOWS_ARTIST)
                .on(ARTIST.ID.eq(USER_FOLLOWS_ARTIST.ARTIST_ID), USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(authUid))
                .where(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_ID.eq(timetableItemRecord.id))
                .fetch()

            // todo equality of records is questionable
            if (mapOfScenePerformances.containsKey(sceneOfTimetableItem)) {
                mapOfScenePerformances[sceneOfTimetableItem]?.add(timetableItemRecord to artistsForTimetableItem.map {
                    Triple(
                        it.into(ARTIST),
                        it.into(COUNTRY),
                        it.into(USER_FOLLOWS_ARTIST).userProfileUid != null
                    )
                }.toList())
            } else {
                mapOfScenePerformances[sceneOfTimetableItem] =
                    mutableListOf(timetableItemRecord to artistsForTimetableItem.map {
                        Triple(
                            it.into(ARTIST),
                            it.into(COUNTRY),
                            it.into(USER_FOLLOWS_ARTIST).userProfileUid != null
                        )
                    }.toList())
            }
        }

        return mapOfScenePerformances.map {
            timetableConverters.createTimetableForSceneDto(it.key, it.value)
        }.toList()
    }

    override fun getTimetableItemsForEvent(eventId: Long): Set<TimetablePerformanceWriteDto> {
        return dsl
            .select()
            .from(TIMETABLE_ITEM)
            .leftOuterJoin(TIMETABLE_ITEM_PERFORMING_GROUP)
            .on(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_ID.eq(TIMETABLE_ITEM.ID))
            .where(TIMETABLE_ITEM.EVENT_ID.eq(eventId))
            .fetch()
            .map {
                val timetableItem = it.into(TIMETABLE_ITEM)
                val artistIds = dsl
                    .selectFrom(TIMETABLE_ITEM_PERFORMING_GROUP)
                    .where(TIMETABLE_ITEM_PERFORMING_GROUP.TIMETABLE_ITEM_ID.eq(timetableItem.id))
                    .fetch()
                    .map { performingGroup ->
                        performingGroup.into(TIMETABLE_ITEM_PERFORMING_GROUP)
                            .artistId
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
                    this.eventId = id
                    this.createdDateTime = dateTimeProvider.getNow()
                }
                .store()

            performance.artistIds.forEach {
                dsl
                    .newRecord(TIMETABLE_ITEM_PERFORMING_GROUP)
                    .apply {
                        this.artistId = it
                        this.timetableItemId = timetableItem.id
                    }
                    .store()
            }
        }
    }

    override fun updateTimetablePerformancesNotTouchingArtists(id: Long, performances: Set<TimetablePerformanceWriteDto>) {
        performances.forEach { performance ->
            val timetableItem = dsl
                .selectFrom(TIMETABLE_ITEM)
                .where(TIMETABLE_ITEM.ID.eq(performance.id))
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
                .where(TIMETABLE_ITEM.ID.eq(it))
                .execute()
        }
    }
}