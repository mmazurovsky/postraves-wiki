package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.data.dto.TicketPriceDto
import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
import jooq.tables.records.EventRecord
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class EventWriteDto(
    val id: Long? = null,
    val name: String,
    val placeId: Long,
    @Serializable(KOffsetDateTimeSerializer::class)
    val startDateTime: OffsetDateTime,
    @Serializable(KOffsetDateTimeSerializer::class)
    val endDateTime: OffsetDateTime,
    val ticketPrices: Collection<TicketPriceDto>,
    val organizers: Set<Long>,
    val imageLink : String? = null,
    val about: String? = null,
    val ticketsLink: String? = null,
) : BaseWriteDto