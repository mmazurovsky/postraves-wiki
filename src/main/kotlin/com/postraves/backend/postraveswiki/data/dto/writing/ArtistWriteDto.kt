package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import jooq.tables.records.ArtistRecord
import kotlinx.serialization.Serializable

@Serializable
data class ArtistWriteDto(
    val id: Long?,
    val name: String,
    val imageLink : String?,
    val countryName: String?,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val about: String?,
    val soundcloudFollowersCount: Int?,
) : BaseWriteDto {

    fun convertToDbRecord() : ArtistRecord {
        return ArtistRecord(
            id = id,
            name = name,
            imageLink = imageLink,
            countryName = countryName,
            soundcloudLink = soundcloudLink,
            instagramLink = instagramLink,
            about = about,
            baseRating = if (id != null) null else soundcloudFollowersCount?.div(5) ?: 0,
            overallFollowersCount = if (id != null) null else 0,
            )
    }
}