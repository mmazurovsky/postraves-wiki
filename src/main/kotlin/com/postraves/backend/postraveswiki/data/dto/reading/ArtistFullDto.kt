package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.Serializable

@Serializable
data class ArtistFullDto(
    val id: Long,
    val name: String,
    val baseRating: Int,
    val overallFollowersCount: Int,
    val imageLink : String?,
    val country: CountryDto?,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val about: String?,
//    val unitiesShort: List<UnityShortForArtistDto> = ArrayList<>()
//    val eventsShort: List<EventShortDto>
) : BaseFullDto {
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(artistRecord: ArtistRecord?, countryRecord: CountryRecord?) : ArtistFullDto {
            return ArtistFullDto(
                id = artistRecord?.id ?: throw TODO(),
                name = artistRecord.name ?: throw TODO(),
                baseRating = artistRecord.baseRating ?: throw TODO(),
                overallFollowersCount = artistRecord.overallFollowersCount ?: throw TODO(),
                imageLink = artistRecord.imageLink,
                instagramLink = artistRecord.instagramLink,
                soundcloudLink = artistRecord.soundcloudLink,
                about = artistRecord.about,
                country =
                if (countryRecord?.name != null)
                    CountryDto(
                    name = countryRecord.name ?: throw TODO(),
                    phoneCode = countryRecord.phoneCode ?: throw TODO(),
                    emojiCode = countryRecord.emojiCode ?: throw TODO())
                else null
            )
        }
    }
}
