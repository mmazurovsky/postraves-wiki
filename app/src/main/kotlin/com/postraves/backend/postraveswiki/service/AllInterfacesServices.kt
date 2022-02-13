package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.*
import com.postraves.backend.postraveswiki.data.dto.reading.EventShortDto

interface BaseService<WRITEDTO : BaseWriteDto, SHORTDTO : BaseShortDto> {
    fun save(dto: WRITEDTO): SHORTDTO
    fun saveBatch(list: List<WRITEDTO>): List<SHORTDTO>
    fun update(dto: WRITEDTO)
    fun findAll(): List<SHORTDTO>
}

interface FindByName<SHORTDTO : BaseShortDto> {
    fun findByPartOfName(namePart: String): List<SHORTDTO>
}

interface ByIdService<FULLDTO : FollowableFullDto<FULLDTO>, SHORTDTO : FollowableShortDto<SHORTDTO>> {
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long)
}

interface FollowableService<FULLDTO: FollowableFullDto<FULLDTO>, SHORTDTO: FollowableShortDto<SHORTDTO>> {
    fun incrementFollowers(id: Long)
    fun decrementFollowers(id: Long)
    fun incrementFollowersUnsafe(id: Long)
    fun decrementFollowersUnsafe(id: Long)
    fun enrichWithFollowersCalculationRequired(dto: SHORTDTO): SHORTDTO
    fun enrichWithFollowersCalculationRequired(dto: FULLDTO): FULLDTO
    fun enrichListWithFollowersAndSortByOverallFollowers(list: List<SHORTDTO>): List<SHORTDTO>
    fun enrichListWithFollowers(list: List<SHORTDTO>): List<SHORTDTO>
}

interface FollowableWikiService {
    fun updateImageLink(id: Long, imageBytes: ByteArray)
}

interface RatingsService<FULLDTO: FollowableFullDto<FULLDTO>, SHORTDTO: FollowableShortDto<SHORTDTO>> {
//    fun findListByIds(ids: Set<Long>): List<SHORTDTO>
    fun findOverallRatingInCountryForCity(cityName: String, maxQuantity: Int): List<SHORTDTO>
    fun findWeeklyRatingInCountryForCity(cityName: String, maxQuantity: Int): List<SHORTDTO>
    fun findBestOfTheWeekByCityInCountry(cityName: String): SHORTDTO?
    fun setBestOfTheWeekForAllCities()
    fun removeBestOfTheWeekByCityInCountry(cityName: String)
}

interface ServiceByName<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO
    fun deleteByName(name: String)
}
