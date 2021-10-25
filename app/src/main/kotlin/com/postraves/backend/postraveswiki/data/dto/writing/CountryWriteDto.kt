package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import kotlinx.serialization.*

@Serializable
data class CountryWriteDto(
    val name: String,
    val nameRu: String,
    val nameEn: String,
    val nameDe: String?,
    val nameFr: String?,
    val phoneCode: String,
) : BaseWriteDto

