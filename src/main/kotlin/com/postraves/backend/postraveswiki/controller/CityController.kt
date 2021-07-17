package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.CityDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.service.ArtistService
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.CountryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/city")
class CityController(private val cityService: CityService)
    : BaseRequests<CityWriteDto, CityDto, CityDto>, ByNameRequests<CityDto, CityDto> {

    override fun save(dto: CityWriteDto): CityDto {
        return cityService.save(dto)
    }

    override fun update(dto: CityWriteDto): CityDto {
        return cityService.update(dto)
    }

    override fun findByName(name: String): CityDto {
        return cityService.findByName(name)
    }

    override fun findAll(): List<CityDto> {
        return cityService.findAll()
    }

    override fun deleteByName(name: String): CityDto {
        return cityService.deleteByName(name)
    }
}