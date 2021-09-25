package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import jooq.tables.records.CityRecord
import kotlinx.serialization.*

@Serializable
data class CityWriteDto(
    val name: String,
    val nameRu: String,
    val nameEn: String,
    val nameDe: String,
    val nameFr: String,
    val countryName: String,
    val timeOffset: Int
) : BaseWriteDto

