package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.FollowableFullDto
import com.postraves.backend.postraveswiki.data.dto.TicketPriceDto
import com.postraves.backend.postraveswiki.data.enum.EventStatus
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
import jooq.tables.records.*
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class EventFullDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    val about: String?,
    val ticketsLink: String?,
    val ticketPrices: List<TicketPriceDto>,
    val place: PlaceShortDto,
    @Serializable(KOffsetDateTimeSerializer::class)
    val startDateTime: OffsetDateTime,
    @Serializable(KOffsetDateTimeSerializer::class)
    val endDateTime: OffsetDateTime?,
    val status: EventStatus? = null,
    val isFollowed: Boolean = false,
    override val overallFollowers: Int = 0,
    override val weeklyFollowers: Int = 0,
    ) : FollowableFullDto<EventFullDto> {

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): EventFullDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}
