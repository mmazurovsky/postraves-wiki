package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.MoneyCurrencyDto
import kotlinx.serialization.Serializable

@Serializable
data class TicketPriceDto(
    val name: String?,
    val price: Double,
    val currency: MoneyCurrencyDto
) : BaseShortDto, BaseFullDto
