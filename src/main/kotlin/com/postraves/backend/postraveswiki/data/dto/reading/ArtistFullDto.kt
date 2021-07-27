package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDtoWithIdAndRating
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import kotlinx.serialization.properties.encodeToStringMap

@Serializable
data class ArtistFullDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    val country: CountryDto?,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val about: String?,
    val isFollowed: Boolean = false,
    override val overallFollowers: Int = 0,
    override val weeklyFollowers: Int = 0,
    ) : BaseFullDtoWithIdAndRating<ArtistFullDto> {
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(artistRecord: ArtistRecord, countryRecord: CountryRecord, isFollowed: Boolean) : ArtistFullDto {
            return ArtistFullDto(
                id = artistRecord.id ?: throw RecordFieldNullException("Artist Id"),
                name = artistRecord.name ?: throw RecordFieldNullException("Artist Name"),
                imageLink = artistRecord.imageLink,
                instagramLink = artistRecord.instagramLink,
                soundcloudLink = artistRecord.soundcloudLink,
                about = artistRecord.about,
                country =
                if (countryRecord.name != null)
                    CountryDto.createOutOfDbRecords(countryRecord)
                else null,
                isFollowed = isFollowed
            )
        }

        @ExperimentalSerializationApi
        fun fromMap(map: Map<String, String>): ArtistFullDto =
            Properties.decodeFromStringMap(map)
    }

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): ArtistFullDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> {
        return Properties.encodeToStringMap(value = this)
    }
}
