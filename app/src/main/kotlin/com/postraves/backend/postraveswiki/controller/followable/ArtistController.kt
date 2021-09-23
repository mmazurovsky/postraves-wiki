package com.postraves.backend.postraveswiki.controller.followable

import com.postraves.backend.postraveswiki.controller.*
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.EventShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.EventService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/artist")
class ArtistController (
    private val artistService: ArtistService,
    private val eventService: EventService,
    ) :
    BaseRequests<ArtistWriteDto, ArtistShortDto>,
    ByIdRequests<ArtistFullDto>,
    RatingRequests<ArtistShortDto>,
    FindByNameRequests<ArtistShortDto>,
    RelevantEventsRequests {

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
        return artistService.findOverallRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<ArtistShortDto> {
        return artistService.findWeeklyRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findByPartOfName(namePart: String): List<ArtistShortDto> {
        return artistService.findByPartOfName(namePart)
    }

    override fun getRelevantEvents(id: Long): List<EventShortDto> {
        return eventService.getRelevantEventsForArtist(id)
    }

    override fun saveBatch(list: List<ArtistWriteDto>): List<ArtistShortDto> {
        return artistService.saveBatch(list)
    }

    @GetMapping("/public/{id}/unities")
    @ResponseStatus(HttpStatus.OK)
    fun getUnitiesOfArtist(@PathVariable id: Long): List<UnityShortDto> {
        return artistService.getUnitiesOfArtist(id)
    }
}