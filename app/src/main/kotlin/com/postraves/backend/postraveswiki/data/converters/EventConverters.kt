package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.TicketPriceDto
import com.postraves.backend.postraveswiki.data.dto.reading.EventFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.EventShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.EventWriteDto
import com.postraves.backend.postraveswiki.data.enum.EventStatus
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import jooq.tables.records.*
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

interface EventConverters {
    fun createShortDtoFromRecords(eventRecord: EventRecord, ticketPrices: List<TicketPriceRecord>, placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isPlaceFollowed: Boolean, isEventFollowed: Boolean): EventShortDto
    fun createFullDtoFromRecords(eventRecord: EventRecord, ticketPrices: List<TicketPriceRecord>, placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isPlaceFollowed: Boolean, isEventFollowed: Boolean): EventFullDto
    fun transferDataFromDtoToRecord(dto: EventWriteDto, record: EventRecord)
}

@Service
class EventConvertersImpl(
    private val placeConverters: PlaceConverters,
    private val dateTimeProvider: DateTimeProvider,
    ) : EventConverters {

    override fun createShortDtoFromRecords(eventRecord: EventRecord, ticketPrices: List<TicketPriceRecord>, placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isPlaceFollowed: Boolean, isEventFollowed: Boolean) : EventShortDto {

        val placeOfEvent = placeConverters.createShortDtoFromRecord(placeRecord, cityRecord, countryRecord, isPlaceFollowed)
        val offsetFromUtcForThePlace = cityRecord.cityTimeOffset ?: throw RecordFieldNullException("City time offset")
        val startDateTimeWithTimeZone = eventRecord.eventStartDateTime?.toInstant()?.atOffset(ZoneOffset.ofHours(offsetFromUtcForThePlace)) ?: throw RecordFieldNullException("Event start date time")
        val eventStatus = resolveEventStatus(eventRecord)
        val ticketPricesDtos = ticketPrices.map { TicketPriceDto.createOutOfDbRecords(it) }.toList()

        return EventShortDto(
            id = eventRecord.eventId ?: throw RecordFieldNullException("Event Id"),
            name = eventRecord.eventName ?: throw RecordFieldNullException("Event Name"),
            imageLink = eventRecord.eventImageLink,
            ticketPrices = ticketPricesDtos,
            place = placeOfEvent,
            startDateTime = startDateTimeWithTimeZone,
            isFollowed = isEventFollowed,
            status = eventStatus
        )
    }

    override fun createFullDtoFromRecords(eventRecord: EventRecord, ticketPrices: List<TicketPriceRecord>, placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isPlaceFollowed: Boolean, isEventFollowed: Boolean) : EventFullDto {

        val placeOfEvent = placeConverters.createShortDtoFromRecord(placeRecord, cityRecord, countryRecord, isPlaceFollowed)
        val offsetFromUtcForThePlace = cityRecord.cityTimeOffset ?: throw RecordFieldNullException("City time offset")
        val startDateTimeWithTimeZone = eventRecord.eventStartDateTime?.toInstant()?.atOffset(ZoneOffset.ofHours(offsetFromUtcForThePlace)) ?: throw RecordFieldNullException("Event start date time")
        val endDateTimeWithTimeZone = eventRecord.eventEndDateTime?.toInstant()?.atOffset(ZoneOffset.ofHours(offsetFromUtcForThePlace)) ?: throw RecordFieldNullException("Event end date time")
        val eventStatus = resolveEventStatus(eventRecord)
        val ticketPricesDtos = ticketPrices.map { TicketPriceDto.createOutOfDbRecords(it) }.toList()


        return EventFullDto(
            id = eventRecord.eventId ?: throw RecordFieldNullException("Event Id"),
            name = eventRecord.eventName ?: throw RecordFieldNullException("Event Name"),
            imageLink = eventRecord.eventImageLink,
            about = eventRecord.eventAbout,
            ticketsLink = eventRecord.eventTicketsLink,
            ticketPrices = ticketPricesDtos,
            place = placeOfEvent,
            startDateTime = startDateTimeWithTimeZone,
            endDateTime = endDateTimeWithTimeZone,
            isFollowed = isEventFollowed,
            status = eventStatus,
        )
    }

    override fun transferDataFromDtoToRecord(dto: EventWriteDto, record: EventRecord) {
        record.eventName = dto.name
        record.eventImageLink = dto.imageLink
        record.eventAbout = dto.about
        record.eventTicketsLink = dto.ticketsLink
        record.eventStartDateTime = dto.startDateTime
        record.eventEndDateTime = dto.endDateTime
        record.eventPlaceId = dto.placeId
    }

    private fun resolveEventStatus(eventRecord: EventRecord) : EventStatus {
        val nowDateTime = dateTimeProvider.getNow()
        val startDateTime = eventRecord.eventStartDateTime ?: throw RecordFieldNullException("Event start date time")
        val endDateTime = eventRecord.eventEndDateTime ?: throw RecordFieldNullException("Event end date time")
        val ticketsLink = eventRecord.eventTicketsLink
        val isCancelled = eventRecord.eventIsCancelled ?: throw RecordFieldNullException("Event is cancelled")

        return resolveEventStatus(nowDateTime, startDateTime, endDateTime, isCancelled, ticketsLink)
    }

    private fun resolveEventStatus(nowDateTime: OffsetDateTime, startDateTime: OffsetDateTime, endDateTime: OffsetDateTime, isEventCancelled: Boolean, ticketsLink: String?): EventStatus {
        val daysBetweenNowAndEventStartTime = ChronoUnit.DAYS.between(nowDateTime, startDateTime)
        if (isEventCancelled) {
            return EventStatus.CANCELLED
        } else if (daysBetweenNowAndEventStartTime > 1) {
            return if (ticketsLink == null) {
                EventStatus.UPCOMING
            } else {
                EventStatus.PRESALE
            }
        } else if (daysBetweenNowAndEventStartTime > 0) {
            return EventStatus.TOMORROW
        } else if (daysBetweenNowAndEventStartTime == 0.toLong()) {
            return if ((nowDateTime.isEqual(startDateTime) || nowDateTime.isAfter(startDateTime)) && nowDateTime.isBefore(endDateTime)) {
                EventStatus.LIVE
            } else if (nowDateTime.isBefore(startDateTime)) {
                EventStatus.TODAY
            } else EventStatus.PAST
        } else return EventStatus.PAST
    }
}