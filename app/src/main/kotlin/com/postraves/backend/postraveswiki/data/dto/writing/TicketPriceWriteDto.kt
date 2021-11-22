package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import kotlinx.serialization.Serializable

@Serializable
data class TicketPriceWriteDto(
    val name: String?,
    val price: Double,
    val currency: String
) : BaseWriteDto
