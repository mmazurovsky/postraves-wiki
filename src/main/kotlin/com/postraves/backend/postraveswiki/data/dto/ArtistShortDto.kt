package com.postraves.backend.postraveswiki.data.dto

import com.postraves.backend.postraveswiki.data.projection.BaseProjection

data class ArtistShortDto(
    val id: Long,
    val name: String,
    val imageLink : String?,
    val rating: Int,
    val country: CountryDto?,
) : BaseDto