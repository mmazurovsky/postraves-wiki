package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.ConvertableToMap
import com.postraves.backend.postraveswiki.data.dto.FollowableShortDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.FollowableFullDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CountryRecord
import jooq.tables.records.UnityRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
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
    val isFollowed: Boolean = false,
    override val overallFollowers: Int = 0,
    override val weeklyFollowers: Int = 0,
    ) : FollowableFullDto<UnityFullDto>, ConvertableToMap {
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(unityRecord: UnityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : UnityFullDto {
            return UnityFullDto(
                id = unityRecord.id ?: throw RecordFieldNullException("Unity Id"),
                name = unityRecord.name ?: throw RecordFieldNullException("Unity Name"),
                imageLink = unityRecord.imageLink,
                instagramLink = unityRecord.instagramLink,
                soundcloudLink = unityRecord.soundcloudLink,
                bandcampLink = unityRecord.bandcampLink,
                about = unityRecord.about,
                country =
                if (countryRecord.name != null)
                    CountryDto.createOutOfDbRecords(countryRecord)
                else null,
                isFollowed = isFollowed
            )
        }

        @ExperimentalSerializationApi
        fun fromMap(map: Map<String, String>): UnityFullDto =
            Properties.decodeFromStringMap(map)
    }

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): UnityFullDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> {
        return Properties.encodeToStringMap(value = this)
    }
}
