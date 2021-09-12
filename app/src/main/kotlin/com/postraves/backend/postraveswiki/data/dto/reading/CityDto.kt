package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.*

@Serializable
data class CityDto(
    val name: String,
    val nameRu: String,
    val nameUk: String,
    val nameDe: String,
    val nameFr: String,
    val country: CountryDto,
) : BaseShortDto, BaseFullDto
