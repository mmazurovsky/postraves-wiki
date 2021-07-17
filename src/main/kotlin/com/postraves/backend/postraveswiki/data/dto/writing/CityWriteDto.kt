package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.*

@Serializable
data class CityWriteDto(
    val name: String,
    val countryName: String,
    val timeOffset: Int
) : BaseWriteDto {

    fun transferDataToDbRecord(record: CityRecord) {
        record.name = name
        record.countryName = countryName
        record.timeOffset = timeOffset
    }
}

