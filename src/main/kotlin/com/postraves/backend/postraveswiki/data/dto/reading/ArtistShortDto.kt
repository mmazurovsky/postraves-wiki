package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto

data class ArtistShortDto(
    val id: Long,
    val name: String,
    val imageLink : String?,
    val rating: Int,
    val country: CountryDto?,
) : BaseShortDto