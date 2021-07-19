package com.postraves.backend.postraveswiki.service.generic

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.repo.generic.RatingRepo
import org.springframework.stereotype.Service

interface RatingService<SHORTDTO: BaseShortDto> {
    fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
    fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
    fun findOfTheWeekInCountry(countryName: String): SHORTDTO
    fun changeBaseRating(id: Long, socialMediaFollowersCount: Int)
}
