package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto

interface BaseService<WRITEDTO : BaseWriteDto, SHORTDTO : BaseShortDto> {
    fun save(dto: WRITEDTO) : SHORTDTO
    fun update(dto: WRITEDTO)
    fun findAll(): List<SHORTDTO>
}

interface FindByName<SHORTDTO : BaseShortDto> {
    fun findByPartOfName(namePart: String): List<SHORTDTO>
}

interface ByIdService<FULLDTO : BaseFullDto, SHORTDTO : BaseShortDto> {
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long)
}

interface RatingService<SHORTDTO: BaseShortDto> {
    fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO>
    fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO>
    fun findBestOfTheWeekByCityInCountry(cityName: String): SHORTDTO
    fun incrementFollowers(id: Long)
    fun decrementFollowers(id: Long)
}

interface ServiceByName<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO
    fun deleteByName(name: String)
}

