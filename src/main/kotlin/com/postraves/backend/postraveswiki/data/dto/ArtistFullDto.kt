package com.postraves.backend.postraveswiki.data.dto

data class ArtistFullDto(
    val id: Long,
    val name: String,
    val imageLink : String?,
    val rating: Int,
    val country: CountryDto?,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val about: String?,
//    val unitiesShort: List<UnityShortForArtistDto> = ArrayList<>()
//    val eventsShort: List<EventShortDto>
) : BaseFullDto
