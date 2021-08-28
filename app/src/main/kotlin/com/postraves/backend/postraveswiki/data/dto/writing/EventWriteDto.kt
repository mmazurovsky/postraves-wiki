package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.data.dto.TicketPriceDto
import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
import jooq.tables.records.EventRecord
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class EventWriteDto(
    val id: Long?,
    val name: String,
    val imageLink : String?,
    val about: String?,
    val ticketsLink: String?,
    val ticketPrices: List<TicketPriceDto>,
    @Serializable(KOffsetDateTimeSerializer::class)
    val startDateTime: OffsetDateTime,
    @Serializable(KOffsetDateTimeSerializer::class)
    val endDateTime: OffsetDateTime?,
    val placeId: Long,
) : BaseWriteDto {

    fun transferDataToDbRecord(record: EventRecord) {
        record.name = name
        record.imageLink = imageLink
        record.about = about
        record.ticketsLink = ticketsLink
        record.startDateTime = startDateTime
        record.endDateTime = endDateTime
        record.placeId = placeId
        //todo ticket prices
        //todo datetimes??
    }
}