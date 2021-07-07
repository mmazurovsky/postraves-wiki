package com.postraves.backend.postraveswiki.data.dto

data class ArtistShortDto(
    val id: Long,
    val name: String,
    val imageLink : String?,
    val rating: Int,
    val country: CountryDto?,
) : BaseShortDto