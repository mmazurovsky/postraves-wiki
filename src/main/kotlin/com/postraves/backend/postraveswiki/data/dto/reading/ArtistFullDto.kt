package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDtoWithId
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.Serializable

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
    val overallFollowers: Int = 0,
    val weeklyFollowers: Int = 0,
    ) : BaseFullDtoWithId {
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(artistRecord: ArtistRecord, countryRecord: CountryRecord, isFollowed: Boolean) : ArtistFullDto {
            return ArtistFullDto(
                id = artistRecord.id ?: throw TODO(),
                name = artistRecord.name ?: throw TODO(),
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
    }

//    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): ArtistFullDto {
//        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
//    }

//        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)

}
