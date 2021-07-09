package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.service.ArtistService
import com.postraves.backend.postraveswiki.service.CountryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/country")
class CountryController(private val countryService: CountryService) : BaseRequests<CountryDto, CountryDto, CountryDto> {

    override fun save(dto: CountryDto): ResponseEntity<CountryDto> {
        return ResponseEntity.ok(countryService.save(dto))
    }

    override fun update(dto: CountryDto): ResponseEntity<CountryDto> {
        return ResponseEntity.ok(countryService.update(dto))
    }

    @GetMapping("/public/{name}")
    fun findByName(@PathVariable name: String): ResponseEntity<CountryDto> {
        return ResponseEntity.ok(countryService.findByName(name))
    }

    override fun findAll(): ResponseEntity<List<CountryDto>> {
        return ResponseEntity.ok(countryService.findAll())
    }

    @DeleteMapping("/{name}")
    fun deleteByName(@PathVariable name: String): ResponseEntity<CountryDto> {
        return ResponseEntity.ok(countryService.deleteByName(name))
    }
}