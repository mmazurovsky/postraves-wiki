package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.ConvertableToMap
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.data.dto.FollowableFullDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.encodeToStringMap

@Serializable
data class UnityFullDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    val country: CountryDto?,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val bandcampLink: String?,
    val about: String?,
    @Required
    val isFollowed: Boolean = false,
    @Required
    override val overallFollowers: Int = 0,
    @Required
    override val weeklyFollowers: Int = 0,
    ) : FollowableFullDto<UnityFullDto>, ConvertableToMap {

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): UnityFullDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> {
        return Properties.encodeToStringMap(value = this)
    }
}
