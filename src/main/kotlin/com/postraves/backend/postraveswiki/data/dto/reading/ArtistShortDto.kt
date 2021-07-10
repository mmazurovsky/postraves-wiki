package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.Serializable

@Serializable
data class ArtistShortDto(
    val id: Long,
    val name: String,
    val imageLink : String?,
    val baseRating: Int,
    val overallFollowersCount: Int,
    val country: CountryDto?,
) : BaseShortDto {
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(artistRecord: ArtistRecord?, countryRecord: CountryRecord?) : ArtistShortDto {
            return ArtistShortDto(
                id = artistRecord?.id ?: throw TODO(),
                name = artistRecord.name ?: throw TODO(),
                baseRating = artistRecord.baseRating ?: throw TODO(),
                overallFollowersCount = artistRecord.overallFollowersCount ?: throw TODO(),
                imageLink = artistRecord.imageLink,
                country =
                if (countryRecord?.name != null) CountryDto(
                    name = countryRecord.name ?: throw TODO(),
                    phoneCode = countryRecord.phoneCode ?: throw TODO(),
                    emojiCode = countryRecord.emojiCode ?: throw TODO())
                else null
            )
        }
    }
}