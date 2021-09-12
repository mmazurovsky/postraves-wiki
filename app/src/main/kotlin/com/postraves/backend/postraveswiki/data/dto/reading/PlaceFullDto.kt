package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.*
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import jooq.tables.records.PlaceRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import kotlinx.serialization.properties.encodeToStringMap

@Serializable
data class PlaceFullDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    val city: CityDto,
    val streetAddress: String,
    val coordinate: CoordinateDto,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val about: String?,
    @Required
    val isFollowed: Boolean = false,
    @Required
    override val overallFollowers: Int = 0,
    @Required
    override val weeklyFollowers: Int = 0,
    ) : FollowableFullDto<PlaceFullDto>, ConvertableToMap {

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> = Properties.encodeToStringMap(value = this)

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): PlaceFullDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}