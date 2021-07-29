package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/artist")
class ArtistController (
    private val artistService: ArtistService
    ) :
    BaseRequests<ArtistWriteDto, ArtistShortDto>,
    ByIdRequests<ArtistFullDto>,
    RatingRequests<ArtistShortDto>,
    FindByNameRequests<ArtistShortDto> {

    override fun save(dto: ArtistWriteDto): ArtistShortDto {
        return artistService.save(dto)
    }

    override fun update(dto: ArtistWriteDto) {
        artistService.update(dto)
    }

    override fun findById(id: Long): ArtistFullDto {
        return artistService.findById(id)
    }

    override fun findAll(): List<ArtistShortDto> {
        return artistService.findAll()
    }

    override fun deleteById(id: Long) {
        artistService.deleteById(id)
    }

    override fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        return artistService.findOverallRatingForCityByCountry(cityName, maxQuantity)
    }

    override fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        return artistService.findWeeklyRatingForCityByCountry(cityName, maxQuantity)
    }

    override fun findByPartOfName(namePart: String): List<ArtistShortDto> {
        return artistService.findByPartOfName(namePart)
    }
}