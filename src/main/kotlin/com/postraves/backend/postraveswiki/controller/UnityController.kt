package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.UnityFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UnityWriteDto
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.UnityService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/unity")
class UnityController (
    private val thisService: UnityService
    ) :
    BaseRequests<UnityWriteDto, UnityShortDto>,
    ByIdRequests<UnityFullDto>,
    RatingRequests<UnityShortDto>,
    FindByNameRequests<UnityShortDto> {

    override fun save(dto: UnityWriteDto): UnityShortDto {
        return thisService.save(dto)
    }

    override fun update(dto: UnityWriteDto) {
        thisService.update(dto)
    }

    override fun findById(id: Long): UnityFullDto {
        return thisService.findById(id)
    }

    override fun findAll(): List<UnityShortDto> {
        return thisService.findAll()
    }

    override fun deleteById(id: Long) {
        thisService.deleteById(id)
    }

    override fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<UnityShortDto> {
        return thisService.findOverallRatingForCityByCountry(cityName, maxQuantity)
    }

    override fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<UnityShortDto> {
        return thisService.findWeeklyRatingForCityByCountry(cityName, maxQuantity)
    }

    override fun findByPartOfName(namePart: String): List<UnityShortDto> {
        return thisService.findByPartOfName(namePart)
    }
}