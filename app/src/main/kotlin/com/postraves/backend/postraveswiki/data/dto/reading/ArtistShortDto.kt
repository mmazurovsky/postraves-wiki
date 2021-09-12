package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.*
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import kotlinx.serialization.properties.encodeToStringMap

@Serializable
data class ArtistShortDto(
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
    ) : FollowableShortDto<ArtistShortDto>, ConvertableToMap {

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> = Properties.encodeToStringMap(value = this)

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): ArtistShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}