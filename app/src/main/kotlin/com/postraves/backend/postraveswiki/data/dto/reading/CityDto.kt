package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import kotlinx.serialization.*

@Serializable
data class CityDto(
    val name: String,
    val localName: String,
    val country: CountryDto,
    val timeOffset: Int
) : BaseShortDto, BaseFullDto
