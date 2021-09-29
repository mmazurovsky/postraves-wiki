package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CountryRecord
import kotlinx.serialization.*

@Serializable
data class CountryDto(
    val name: String,
    val localName: String,
    val phoneCode: String,
    val emojiCode: String,
) : BaseShortDto, BaseFullDto

