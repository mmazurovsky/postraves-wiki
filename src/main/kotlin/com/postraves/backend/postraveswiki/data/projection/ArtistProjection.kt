package com.postraves.backend.postraveswiki.data.projection

import com.postraves.backend.postraveswiki.data.dto.ArtistDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto

data class ArtistProjection(
    val id: Long,
    val name: String,
    val imageLink: String?)
    : BaseProjection<ArtistDto> {

    override fun convertToDto() : ArtistDto {
        return ArtistDto(
            id = id,
            name = name,
            imageLink = imageLink,
//            rating = getRating(),
//            country = CountryDto(
//                name = getCountry().getName(),
//                phoneCode = getCountry().getPhoneCode(),
//                emojiCode = getCountry().getEmojiCode()
//            ),
//            about = getAbout(),
//            soundcloudLink = getSoundcloudLink(),
//            instagramLink = getInstagramLink()
        )
    }
}