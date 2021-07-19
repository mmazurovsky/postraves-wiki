package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.service.ArtistService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/artist")
class ArtistController
    (
    private val artistService: ArtistService
)
    : BaseRequests<ArtistWriteDto, ArtistShortDto>, ByIdRequests<ArtistFullDto>, RatingRequests<ArtistShortDto> {

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

    override fun findOverallRating(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        //todo change city to country in service
        return artistService.findOverallTopInCountry(cityName, maxQuantity)
    }
}