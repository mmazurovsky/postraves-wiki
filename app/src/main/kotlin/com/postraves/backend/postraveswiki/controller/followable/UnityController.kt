package com.postraves.backend.postraveswiki.controller.followable

import com.postraves.backend.postraveswiki.controller.*
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.EventShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UnityWriteDto
import com.postraves.backend.postraveswiki.service.followable.EventService
import com.postraves.backend.postraveswiki.service.followable.UnityService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/unity")
class UnityController (
    private val thisService: UnityService,
    private val eventService: EventService,
    ) :
    BaseRequests<UnityWriteDto, UnityShortDto>,
    ByIdRequests<UnityFullDto>,
    RatingRequests<UnityShortDto>,
    FindByNameRequests<UnityShortDto>,
    RelevantEventsRequests {

    override fun save(dto: UnityWriteDto): UnityShortDto {
        return thisService.save(dto)
    }

    override fun update(dto: UnityWriteDto) {
        thisService.update(dto)
    }

    override fun findById(id: Long): UnityFullDto {
        return thisService.findById(id)
    }

    override fun findAll(): List<UnityShortDto> {
        return thisService.findAll()
    }

    override fun deleteById(id: Long) {
        thisService.deleteById(id)
    }

    override fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<UnityShortDto> {
        return thisService.findOverallRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findWeeklyRatingForCityByCountry(cityName: String, maxQuantity: Int): List<UnityShortDto> {
        return thisService.findWeeklyRatingInCountryForCity(cityName, maxQuantity)
    }

    override fun findByPartOfName(namePart: String): List<UnityShortDto> {
        return thisService.findByPartOfName(namePart)
    }

    override fun getRelevantEvents(id: Long): List<EventShortDto> {
        return eventService.getRelevantEventsForUnity(id)
    }

    @PutMapping("/{id}/artists")
    @ResponseStatus(HttpStatus.OK)
    fun updateArtistsOfUnity(@PathVariable id: Long, @RequestBody artists: Set<Long>) {
        return thisService.updateArtistsOfUnity(id, artists)
    }

    @GetMapping("/public/{id}/artists")
    @ResponseStatus(HttpStatus.OK)
    fun getArtistsOfUnity(@PathVariable id: Long): List<ArtistShortDto> {
        return thisService.getArtistsOfUnity(id)
    }

    override fun saveBatch(list: List<UnityWriteDto>): List<UnityShortDto> {
        return thisService.saveBatch(list)
    }
}