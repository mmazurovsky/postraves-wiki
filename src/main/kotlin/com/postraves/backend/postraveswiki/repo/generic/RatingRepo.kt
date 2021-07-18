package com.postraves.backend.postraveswiki.repo.generic

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto

interface RatingRepo<SHORTDTO : BaseShortDto> {
    fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
    fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
    fun findOfTheWeekInCountry(countryName: String): SHORTDTO
    fun changeBaseRating(id: Long, newBaseRating: Int)
}
