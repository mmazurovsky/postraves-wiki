package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.data.dto.CoordinateDto
import jooq.tables.records.PlaceRecord
import kotlinx.serialization.Serializable

@Serializable
data class PlaceWriteDto(
    val id: Long?,
    val name: String,
    val imageLink : String?,
    val cityName: String,
    val streetAddress: String,
    val coordinate: CoordinateDto,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val about: String?,
) : BaseWriteDto {

    fun transferDataToDbRecord(record: PlaceRecord) {
        record.name = name
        record.imageLink = imageLink
        record.cityName = cityName
        record.soundcloudLink = soundcloudLink
        record.instagramLink = instagramLink
        record.about = about
        record.latitude = coordinate.latitude
        record.longitude = coordinate.longitude
        record.streetAddress = streetAddress
    }
}