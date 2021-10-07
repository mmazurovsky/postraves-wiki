package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.FollowableFullDto
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class UserFullDto(
    override val id: Long,
    val name: String,
    val currentCity: CityDto,
    val imageLink : String?,
    val about: String?,
    val telegramUsername: String?,
    val instagramUsername: String?,
    @Required
    override val overallFollowers: Int = 0,
    @Required
    override val weeklyFollowers: Int = 0,
) : FollowableFullDto<UserFullDto> {

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): UserFullDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}
