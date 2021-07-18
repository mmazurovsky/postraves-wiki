package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.repo.generic.RatingRepo
import org.springframework.stereotype.Service

interface RatingService<SHORTDTO: BaseShortDto> {
    fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
    fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO>
    fun findOfTheWeekInCountry(countryName: String): SHORTDTO
    fun changeBaseRating(id: Long, socialMediaFollowersCount: Int)
}

@Service
class RatingServiceImpl<SHORTDTO: BaseShortDto,
        REPO : RatingRepo<SHORTDTO>>
    (
    private val ratingRepo: REPO
) : RatingService<SHORTDTO> {

    override fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO> {
        return ratingRepo.findOverallTopInCountry(countryName, maxQuantity)
    }

    override fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<SHORTDTO> {
        return ratingRepo.findWeeklyTopInCountry(countryName, maxQuantity)
    }

    override fun findOfTheWeekInCountry(countryName: String): SHORTDTO {
        return ratingRepo.findOfTheWeekInCountry(countryName)
    }

    override fun changeBaseRating(id: Long, socialMediaFollowersCount: Int) {
        return ratingRepo.changeBaseRating(id, socialMediaFollowersCount.div(5))
    }
}