package com.postraves.backend.postraveswiki.service.generic

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto

interface RatingService<SHORTDTO: BaseShortDto> {
    fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO>
    fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO>
    fun findBestOfTheWeekByCityInCountry(countryName: String): SHORTDTO
//    fun changeBaseRating(id: Long, socialMediaFollowersCount: Int)
    fun incrementFollowers(id: Long)
    fun decrementFollowers(id: Long)
}
