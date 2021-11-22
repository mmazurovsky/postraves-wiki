package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.FollowableShortDto
import com.postraves.backend.postraveswiki.data.enum.EventStatus
import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
import kotlinx.serialization.Required
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
    @Required
    val status: EventStatus? = null,
    @Required
    val isFollowed: Boolean = false,
    @Required
    override val overallFollowers: Int = 0,
    @Required
    override val weeklyFollowers: Int = 0,
    ) : FollowableShortDto<EventShortDto> {

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): EventShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}
