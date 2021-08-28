package com.postraves.backend.postraveswiki.data.dto

import kotlinx.serialization.*

@Serializable
data class CoordinateDto(
    val latitude: Double,
    val longitude: Double,
) : BaseShortDto, BaseFullDto, BaseWriteDto

