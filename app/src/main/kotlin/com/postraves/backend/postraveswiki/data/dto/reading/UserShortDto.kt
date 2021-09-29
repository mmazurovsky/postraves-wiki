package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.FollowableDto
import com.postraves.backend.postraveswiki.data.dto.FollowableFullDto
import com.postraves.backend.postraveswiki.data.dto.FollowableShortDto
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class UserShortDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    @Required
    override val overallFollowers: Int = 0,
    @Required
    override val weeklyFollowers: Int = 0,
) : FollowableShortDto<UserShortDto> {
    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): UserShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}
