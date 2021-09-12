package com.postraves.backend.postraveswiki.data.dto

import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CountryRecord
import kotlinx.serialization.*

@Serializable
data class CountryDto(
    val name: String,
    val nameRu: String,
    val nameUk: String,
    val nameDe: String,
    val nameFr: String,
    val phoneCode: String,
    var emojiCode: String?,
) : BaseShortDto, BaseFullDto, BaseWriteDto

