package com.postraves.backend.postraveswiki.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MoneyCurrencyDto(
    val name: String,
    val symbol: String,
) : BaseShortDto, BaseFullDto, BaseWriteDto