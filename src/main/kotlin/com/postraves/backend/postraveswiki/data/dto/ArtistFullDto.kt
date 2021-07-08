package com.postraves.backend.postraveswiki.data.dto

import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord

data class ArtistFullDto(
    val id: Long,
    val name: String,
    val imageLink : String?,
    val rating: Int,
    val country: CountryDto?,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val about: String?,
//    val unitiesShort: List<UnityShortForArtistDto> = ArrayList<>()
//    val eventsShort: List<EventShortDto>
) : BaseFullDto {
    companion object FactoryFromDbRecord {
        fun createOutOfDbRecords(artistRecord: ArtistRecord?, countryRecord: CountryRecord?) : ArtistFullDto {
            return ArtistFullDto(
                //TODO record id wont be null
                id = artistRecord?.id ?: throw TODO(),
                name = artistRecord.name ?: throw TODO(),
                rating = artistRecord.rating ?: throw TODO(),
                imageLink = artistRecord.imageLink,
                instagramLink = artistRecord.instagramLink,
                soundcloudLink = artistRecord.soundcloudLink,
                about = artistRecord.about,
                country =
                if (countryRecord != null) CountryDto(
                    name = countryRecord.name ?: throw TODO(),
                    phoneCode = countryRecord.phoneCode ?: throw TODO(),
                    emojiCode = countryRecord.emojiCode ?: throw TODO())
                else null
            )
        }
    }
}
