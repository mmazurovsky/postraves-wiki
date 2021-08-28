package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.*
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CountryRecord
import jooq.tables.records.UnityRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import kotlinx.serialization.properties.encodeToStringMap

@Serializable
data class UnityShortDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    val country: CountryDto?,
    val isFollowed: Boolean = false,
    override val overallFollowers: Int = 0,
    override val weeklyFollowers: Int = 0,
    ) : FollowableShortDto<UnityShortDto>, ConvertableToMap {
    companion object {
        fun createOutOfDbRecords(unityRecord: UnityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : UnityShortDto {
            return UnityShortDto(
                id = unityRecord.id ?: throw RecordFieldNullException("Unity Id"),
                name = unityRecord.name ?: throw RecordFieldNullException("Unity Name"),
                imageLink = unityRecord.imageLink,
                country =
                if (countryRecord.name != null)
                    CountryDto.createOutOfDbRecords(countryRecord)
                else null,
                isFollowed = isFollowed
            )
        }

        @ExperimentalSerializationApi
        fun fromMap(map: Map<String, String>): UnityShortDto =
            Properties.decodeFromStringMap(map)
    }

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> = Properties.encodeToStringMap(value = this)

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): UnityShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}