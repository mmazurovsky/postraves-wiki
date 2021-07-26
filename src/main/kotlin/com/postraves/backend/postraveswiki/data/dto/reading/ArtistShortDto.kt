package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.*
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.*
import kotlinx.serialization.properties.Properties.Default.decodeFromStringMap

@Serializable
@ExperimentalSerializationApi
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
        fun createOutOfDbRecords(artistRecord: ArtistRecord, countryRecord: CountryRecord) : ArtistShortDto {
            return ArtistShortDto(
                id = artistRecord.id ?: throw RecordFieldNullException("Artist Id"),
                name = artistRecord.name ?: throw RecordFieldNullException("Artist Name"),
                imageLink = artistRecord.imageLink,
                country =
                if (countryRecord.name != null) CountryDto(
                    name = countryRecord.name ?: throw RecordFieldNullException("Country Name"),
                    phoneCode = countryRecord.phoneCode ?: throw RecordFieldNullException("Country Phone Code"),
                    emojiCode = countryRecord.emojiCode ?: throw RecordFieldNullException("Country Emoji Code"))
                else null,
            )
        }

        fun fromMap(map: Map<String, String>): ArtistShortDto =
            Properties.decodeFromStringMap(map)
    }

    override fun asMap(): Map<String, String> = Properties.encodeToStringMap(this)

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): ArtistShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}