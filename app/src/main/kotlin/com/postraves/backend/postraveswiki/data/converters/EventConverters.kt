package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.TicketPriceDto
import com.postraves.backend.postraveswiki.data.dto.reading.EventFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.EventShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.PlaceShortDto
import com.postraves.backend.postraveswiki.data.enum.EventStatus
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import jooq.tables.records.*
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

interface EventConverters {
    fun createShortDtoFromRecords(eventRecord: EventRecord, ticketPrices: List<TicketPriceRecord>, placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isPlaceFollowed: Boolean, isEventFollowed: Boolean): EventShortDto
    fun createFullDtoFromRecords(eventRecord: EventRecord, ticketPrices: List<TicketPriceRecord>, placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isPlaceFollowed: Boolean, isEventFollowed: Boolean): EventFullDto
}

@Service
class EventConvertersImpl(
    private val placeConverters: PlaceConverters,
    private val dateTimeProvider: DateTimeProvider,
    ) : EventConverters {

    override fun createShortDtoFromRecords(eventRecord: EventRecord, ticketPrices: List<TicketPriceRecord>, placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isPlaceFollowed: Boolean, isEventFollowed: Boolean) : EventShortDto {
        return EventShortDto(
            id = eventRecord.id ?: throw RecordFieldNullException("Event Id"),
            name = eventRecord.name ?: throw RecordFieldNullException("Event Name"),
            imageLink = eventRecord.imageLink,
            ticketPrices = ticketPrices.map { TicketPriceDto.createOutOfDbRecords(it) }.toList(),
            place = placeConverters.createShortDtoFromRecord(placeRecord, cityRecord, countryRecord, isPlaceFollowed),
            startDateTime = eventRecord.startDateTime ?: throw RecordFieldNullException("Event start date time"),
            isFollowed = isEventFollowed,
            status = resolveEventStatus(eventRecord)
        )
    }

    override fun createFullDtoFromRecords(eventRecord: EventRecord, ticketPrices: List<TicketPriceRecord>, placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isPlaceFollowed: Boolean, isEventFollowed: Boolean) : EventFullDto {
        return EventFullDto(
            id = eventRecord.id ?: throw RecordFieldNullException("Event Id"),
            name = eventRecord.name ?: throw RecordFieldNullException("Event Name"),
            imageLink = eventRecord.imageLink,
            about = eventRecord.about,
            ticketsLink = eventRecord.ticketsLink,
            ticketPrices = ticketPrices.map { TicketPriceDto.createOutOfDbRecords(it) }.toList(),
            place = placeConverters.createShortDtoFromRecord(placeRecord, cityRecord, countryRecord, isPlaceFollowed),
            startDateTime = eventRecord.startDateTime ?: throw RecordFieldNullException("Event start date time"),
            endDateTime = eventRecord.endDateTime,
            isFollowed = isEventFollowed,
            status = resolveEventStatus(eventRecord)
        )
    }

    private fun resolveEventStatus(eventRecord: EventRecord) : EventStatus {
        val nowDateTime = dateTimeProvider.getNow()
        val startDateTime = eventRecord.startDateTime ?: throw RecordFieldNullException("Event start date time")
        val endDateTime = eventRecord.endDateTime ?: throw RecordFieldNullException("Event end date time")
        val ticketsLink = eventRecord.ticketsLink
        val isCancelled = eventRecord.isCancelled ?: throw RecordFieldNullException("Event is cancelled")

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