package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.service.CityService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/city")
class CityController
    (private val cityService: CityService) : BaseRequests<CityWriteDto, CityDto>, ByNameRequests<CityDto> {

    override fun save(dto: CityWriteDto): CityDto {
        return cityService.save(dto)
    }

    override fun update(dto: CityWriteDto) {
        cityService.update(dto)
    }

    override fun findByName(name: String): CityDto {
        return cityService.findByName(name)
    }

    override fun findAll(): List<CityDto> {
        return cityService.findAll()
    }

    override fun deleteByName(name: String) {
        cityService.deleteByName(name)
    }
}