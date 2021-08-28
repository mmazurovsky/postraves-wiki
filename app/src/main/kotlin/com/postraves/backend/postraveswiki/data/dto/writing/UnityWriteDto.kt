package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import jooq.tables.records.UnityRecord
import kotlinx.serialization.Serializable

@Serializable
data class UnityWriteDto(
    val id: Long?,
    val name: String,
    val imageLink : String?,
    val countryName: String?,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val bandcampLink: String?,
    val about: String?,
) : BaseWriteDto {

    fun transferDataToDbRecord(record: UnityRecord) {
        record.name = name
        record.imageLink = imageLink
        record.countryName = countryName
        record.soundcloudLink = soundcloudLink
        record.instagramLink = instagramLink
        record.bandcampLink = bandcampLink
        record.about = about
    }
}