package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.*
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.encodeToStringMap

@Serializable
data class UnityShortDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    val country: CountryDto?,
    @Required
    val isFollowed: Boolean = false,
    @Required
    override val overallFollowers: Int = 0,
    @Required
    override val weeklyFollowers: Int = 0,
    ) : FollowableShortDto<UnityShortDto>, ConvertableToMap {

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> = Properties.encodeToStringMap(value = this)

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): UnityShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}