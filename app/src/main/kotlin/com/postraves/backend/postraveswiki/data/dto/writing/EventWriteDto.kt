package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.data.dto.reading.TicketPriceDto
import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
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
    val ticketPrices: List<TicketPriceWriteDto>? = null,
    // todo maybe delete because it is taken into account on save but is ignored on update
    val organizers: Set<Long>? = null,
    val imageLink: String? = null,
    val about: String? = null,
    val ticketsLink: String? = null,
) : BaseWriteDto