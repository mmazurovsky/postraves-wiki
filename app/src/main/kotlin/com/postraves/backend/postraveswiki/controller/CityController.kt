package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.service.CityService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/city")
class CityController(
    private val cityService: CityService
) :
    BaseRequests<CityWriteDto, CityDto>,
    ByNameRequests<CityDto>,
    FindByNameRequests<CityDto> {

    override fun save(dto: CityWriteDto): CityDto {
        return cityService.save(dto)
    }

    override fun update(dto: CityWriteDto) {
        cityService.update(dto)
    }

    override fun findByName(name: String): CityDto {
        return cityService.findByName(name)
    }

    @GetMapping("/public/all")
    override fun findAll(): List<CityDto> {
        return cityService.findAll()
    }

    @GetMapping("/byNameInternal/{name}")
    @ResponseStatus(HttpStatus.OK)
    fun findByNameInternal(@PathVariable name: String): CityWriteDto {
        return cityService.findByNameInternal(name)
    }

    @GetMapping("/allInternal")
    @ResponseStatus(HttpStatus.OK)
    fun findAllInternal(): List<CityWriteDto> {
        return cityService.findAllInternal()
    }

    override fun deleteByName(name: String) {
        cityService.deleteByName(name)
    }

    override fun findByPartOfName(namePart: String): List<CityDto> {
        return cityService.findByPartOfName(namePart)
    }

    override fun saveBatch(list: List<CityWriteDto>): List<CityDto> {
        return cityService.saveBatch(list)
    }
}