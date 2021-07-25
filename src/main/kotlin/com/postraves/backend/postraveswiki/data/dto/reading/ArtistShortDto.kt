package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.*
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.Serializable

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
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(artistRecord: ArtistRecord, countryRecord: CountryRecord) : ArtistShortDto {
            return ArtistShortDto(
                id = artistRecord.id ?: throw TODO(),
                name = artistRecord.name ?: throw TODO(),
                imageLink = artistRecord.imageLink,
                country =
                if (countryRecord.name != null) CountryDto(
                    name = countryRecord.name ?: throw TODO(),
                    phoneCode = countryRecord.phoneCode ?: throw TODO(),
                    emojiCode = countryRecord.emojiCode ?: throw TODO())
                else null,
            )
        }
    }

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): ArtistShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}