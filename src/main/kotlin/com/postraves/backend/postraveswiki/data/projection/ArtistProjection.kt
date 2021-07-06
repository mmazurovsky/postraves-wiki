package com.postraves.backend.postraveswiki.data.projection

import com.postraves.backend.postraveswiki.data.dto.ArtistDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto

interface ArtistProjection : BaseProjection<ArtistDto> {
    fun getId(): Long
    fun getName(): String
    fun getImageLink(): String
    fun getRating(): Int
    fun getAbout(): String
    fun getSoundcloudLink(): String
    fun getInstagramLink(): String
    fun getCountry(): CountryProjection

    interface CountryProjection {
        fun getName(): String
        fun getPhoneCode(): String
        fun getEmojiCode(): String
    }

    override fun convertToDto() : ArtistDto {
        return ArtistDto(
            id = getId(),
            name = getName(),
            imageLink = getImageLink(),
            rating = getRating(),
            country = CountryDto(
                name = getCountry().getName(),
                phoneCode = getCountry().getPhoneCode(),
                emojiCode = getCountry().getEmojiCode()
            ),
            about = getAbout(),
            soundcloudLink = getSoundcloudLink(),
            instagramLink = getInstagramLink()
        )
    }
}