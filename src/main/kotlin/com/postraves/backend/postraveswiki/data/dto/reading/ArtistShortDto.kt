package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.*
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.ExperimentalSerializationApi
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
    val isFollowed: Boolean = false,
    override val overallFollowers: Int = 0,
    override val weeklyFollowers: Int = 0,
    ) : BaseShortDtoWithIdAndRating<ArtistShortDto> {
    companion object {
        fun createOutOfDbRecords(artistRecord: ArtistRecord, countryRecord: CountryRecord, isFollowed: Boolean) : ArtistShortDto {
            return ArtistShortDto(
                id = artistRecord.id ?: throw RecordFieldNullException("Artist Id"),
                name = artistRecord.name ?: throw RecordFieldNullException("Artist Name"),
                imageLink = artistRecord.imageLink,
                country =
                if (countryRecord.name != null)
                    CountryDto.createOutOfDbRecords(countryRecord)
                else null,
                isFollowed = isFollowed
            )
        }

        @ExperimentalSerializationApi
        fun fromMap(map: Map<String, String>): ArtistShortDto =
            Properties.decodeFromStringMap(map)
    }

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> = Properties.encodeToStringMap(value = this)

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): ArtistShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}