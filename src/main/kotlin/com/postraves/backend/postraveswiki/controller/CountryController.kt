package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.service.ArtistService
import com.postraves.backend.postraveswiki.service.CountryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/country")
class CountryController(private val countryService: CountryService)
    : BaseRequests<CountryDto, CountryDto>, ByNameRequests<CountryDto> {

    override fun save(dto: CountryDto): CountryDto {
        return countryService.save(dto)
    }

    override fun update(dto: CountryDto) {
        countryService.update(dto)
    }

    override fun findByName(name: String): CountryDto {
        return countryService.findByName(name)
    }

    override fun findAll(): List<CountryDto> {
        return countryService.findAll()
    }

    override fun deleteByName(name: String) {
        countryService.deleteByName(name)
    }
}