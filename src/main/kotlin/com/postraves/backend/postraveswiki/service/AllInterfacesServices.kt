package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.*

interface BaseService<WRITEDTO : BaseWriteDto, SHORTDTO : BaseShortDto> {
    fun save(dto: WRITEDTO) : SHORTDTO
    fun update(dto: WRITEDTO)
    fun findAll(): List<SHORTDTO>
}

interface FindByName<SHORTDTO : BaseShortDto> {
    fun findByPartOfName(namePart: String): List<SHORTDTO>
}

interface ByIdService<FULLDTO : BaseFullDtoWithId, SHORTDTO : BaseShortDtoWithId> {
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long)
    fun findListByIds(ids: Set<Long>): List<SHORTDTO>
}

interface RatingService<FULLDTO: BaseFullDtoWithIdAndRating<FULLDTO>, SHORTDTO: BaseShortDtoWithIdAndRating<SHORTDTO>> {
    fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO>
    fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO>
    fun findBestOfTheWeekByCityInCountry(cityName: String): SHORTDTO?
    fun incrementFollowers(id: Long)
    fun decrementFollowers(id: Long)
    fun enrichWithFollowersCalculationRequired(dto: SHORTDTO): SHORTDTO
    fun enrichWithFollowersCalculationRequired(dto: FULLDTO): FULLDTO
    fun setBestOfTheWeekForAllCities()
}

interface ServiceByName<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO
    fun deleteByName(name: String)
}

