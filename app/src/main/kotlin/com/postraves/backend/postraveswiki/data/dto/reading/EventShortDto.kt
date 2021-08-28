package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.FollowableShortDto
import com.postraves.backend.postraveswiki.data.dto.TicketPriceDto
import com.postraves.backend.postraveswiki.data.enum.EventStatus
import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class EventShortDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    val ticketPrices: List<TicketPriceDto>,
    val place: PlaceShortDto,
    @Serializable(KOffsetDateTimeSerializer::class)
    val startDateTime: OffsetDateTime,
    val status: EventStatus? = null,
    val isFollowed: Boolean = false,
    override val overallFollowers: Int = 0,
    override val weeklyFollowers: Int = 0,
    ) : FollowableShortDto<EventShortDto> {

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): EventShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}
