package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import jooq.tables.records.ArtistRecord
import jooq.tables.records.UserProfileRecord
import kotlinx.serialization.Serializable

@Serializable
data class UserWriteDto(
    val name: String,
    val imageLink : String?,
    val currentCity: String?,
    val telegramLink: String?,
    val instagramLink: String?,
    val about: String?,
) : BaseWriteDto {

    fun transferDataToDbRecord(record: UserProfileRecord) {
        record.name = name
        record.imageLink = imageLink
        record.cityName = currentCity
        record.telegramLink = telegramLink
        record.instagramLink = instagramLink
        record.about = about
    }
}