package com.postraves.backend.postraveswiki.service.generic

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto

interface RatingService<SHORTDTO: BaseShortDto> {
    fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
    fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
    fun findBestOfTheWeekInCountry(countryName: String): SHORTDTO
    fun changeBaseRating(id: Long, socialMediaFollowersCount: Int)
    fun incrementFollowers(id: Long)
    fun decrementFollowers(id: Long)
}
