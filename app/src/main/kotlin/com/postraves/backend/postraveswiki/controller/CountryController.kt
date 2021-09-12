package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.service.CountryService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/country")
class CountryController (
    private val countryService: CountryService
    ) :
    BaseRequests<CountryDto, CountryDto>,
    ByNameRequests<CountryDto>,
    FindByNameRequests<CountryDto> {

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

    override fun findByPartOfName(namePart: String): List<CountryDto> {
        return countryService.findByPartOfName(namePart)
    }

    override fun saveBatch(list: List<CountryDto>): List<CountryDto> {
        return countryService.saveBatch(list)
    }
}