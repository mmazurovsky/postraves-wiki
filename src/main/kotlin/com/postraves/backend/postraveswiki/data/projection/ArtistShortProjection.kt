//package com.postraves.backend.postraveswiki.data.projection
//
//import com.postraves.backend.postraveswiki.data.dto.ArtistShortDto
//import com.postraves.backend.postraveswiki.data.dto.CountryDto
//
//interface ArtistShortProjection : BaseProjection<ArtistShortDto> {
//    fun getId(): Long
//    fun getName(): String
//    fun getImageLink(): String
//    fun getRating(): Int
//    fun getCountry(): CountryProjection
//
//    interface CountryProjection {
//        fun getName(): String
//        fun getPhoneCode(): String
//        fun getEmojiCode(): String
//    }
//
//    override fun convertToDto() : ArtistShortDto {
//        return ArtistShortDto(
//            id = getId(),
//            name = getName(),
//            imageLink = getImageLink(),
//            rating = getRating(),
//            country = CountryDto.createDtoFromProjection(getCountry())
//        )
//    }
//}