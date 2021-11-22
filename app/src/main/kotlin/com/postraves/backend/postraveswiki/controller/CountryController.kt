package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.CountryDto
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.MoneyCurrencyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/country")
class CountryController (
    private val countryService: CountryService
    ) :
    BaseRequests<CountryWriteDto, CountryDto>,
    ByNameRequests<CountryDto>,
    FindByNameRequests<CountryDto> {

    override fun save(dto: CountryWriteDto): CountryDto {
        return countryService.save(dto)
    }

    override fun update(dto: CountryWriteDto) {
        countryService.update(dto)
    }

    override fun findByName(name: String): CountryDto {
        return countryService.findByName(name)
    }

    @GetMapping("/public/all")
    override fun findAll(): List<CountryDto> {
        return countryService.findAll()
    }

    override fun deleteByName(name: String) {
        countryService.deleteByName(name)
    }

    override fun findByPartOfName(namePart: String): List<CountryDto> {
        return countryService.findByPartOfName(namePart)
    }

    override fun saveBatch(list: List<CountryWriteDto>): List<CountryDto> {
        return countryService.saveBatch(list)
    }
}