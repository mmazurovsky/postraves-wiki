package com.postraves.backend.postraveswiki.repo.generic

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto

interface RatingRepo<SHORTDTO : BaseShortDto> {
    fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
//    fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
//    fun findOfTheWeekInCountry(countryName: String): SHORTDTO
    fun findOverallTopInCountryForUser(authUid: String, countryName: String, maxQuantity: Int): List<SHORTDTO>
//    fun findWeeklyTopInCountryForUser(authUid: String, countryName: String, maxQuantity: Int): List<SHORTDTO>
//    fun findOfTheWeekInCountryForUser(authUid: String, countryName: String): SHORTDTO
    fun changeBaseRating(id: Long, newBaseRating: Int)
    fun incrementOverallFollowers(id: Long)
    fun decrementOverallFollowers(id: Long)
}
