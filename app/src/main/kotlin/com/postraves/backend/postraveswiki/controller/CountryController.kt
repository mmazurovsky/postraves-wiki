package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.CountryDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.MoneyCurrencyService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/country")
class CountryController(
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
    @PreAuthorize("permitAll()")
    override fun findAll(): List<CountryDto> {
        return countryService.findAll()
    }

    @GetMapping("/byNameInternal/{name}")
    @ResponseStatus(HttpStatus.OK)
    fun findByNameInternal(@PathVariable name: String): CountryWriteDto {
        return countryService.findByNameInternal(name)
    }

    @GetMapping("/allInternal")
    @ResponseStatus(HttpStatus.OK)
    fun findAllInternal(): List<CountryWriteDto> {
        return countryService.findAllInternal()
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