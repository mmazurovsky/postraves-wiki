package com.postraves.backend.postraveswiki.controller.followable

import com.postraves.backend.postraveswiki.controller.BaseRequests
import com.postraves.backend.postraveswiki.controller.ByIdRequests
import com.postraves.backend.postraveswiki.controller.FindByNameRequests
import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.EventWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.TimetablePerformanceWriteDto
import com.postraves.backend.postraveswiki.service.followable.EventService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/event")
class EventController(
    private val eventService: EventService
) :
    BaseRequests<EventWriteDto, EventShortDto>,
    ByIdRequests<EventFullDto>,
    FindByNameRequests<EventShortDto> {

    override fun save(dto: EventWriteDto): EventShortDto {
        return eventService.save(dto)
    }

    override fun update(dto: EventWriteDto) {
        eventService.update(dto)
    }

    override fun findById(id: Long): EventFullDto {
        return eventService.findById(id)
    }

    override fun findAll(): List<EventShortDto> {
        return eventService.findAll()
    }

    override fun deleteById(id: Long) {
        eventService.deleteById(id)
    }

    override fun findByPartOfName(namePart: String): List<EventShortDto> {
        return eventService.findByPartOfName(namePart)
    }

    @GetMapping("/public/relevantByDate")
    @ResponseStatus(HttpStatus.OK)
    fun getEventsForCitySortedByDate(@RequestParam cityName: String): List<EventsByDateDto> {
        return eventService.getEventsByDate(cityName)
    }

    @GetMapping("/public/relevantByRating")
    @ResponseStatus(HttpStatus.OK)
    fun getEventsForCitySortedByRating(@RequestParam cityName: String): List<EventShortDto> {
        return eventService.getEventsByRating(cityName)
    }

    @PutMapping("/{id}/organizers")
    @ResponseStatus(HttpStatus.OK)
    fun updateOrganizers(@PathVariable id: Long, @RequestBody orgs: Set<Long>) {
        eventService.updateOrganizers(id, orgs)
    }

    @GetMapping("/public/{id}/organizers")
    @ResponseStatus(HttpStatus.OK)
    fun getOrganizers(@PathVariable id: Long): List<UnityShortDto> {
        return eventService.getOrganizers(id)
    }

    @PutMapping("/{id}/timetable")
    @ResponseStatus(HttpStatus.OK)
    fun updateTimetable(@PathVariable id: Long, @RequestBody performances: Set<TimetablePerformanceWriteDto>) {
        eventService.updateTimetableForEvent(id, performances)
    }

    @GetMapping("/public/{id}/lineup")
    @ResponseStatus(HttpStatus.OK)
    fun getLineup(@PathVariable id: Long): List<ArtistShortDto> {
        return eventService.getLineup(id)
    }

    @GetMapping("/public/{id}/timetable")
    @ResponseStatus(HttpStatus.OK)
    fun getTimetable(
        @PathVariable id: Long,
        @RequestParam(required = false) isForAdmin: Boolean?
    ): List<TimetableForSceneDto> {
        return eventService.getTimetableForEvent(id, isForAdmin ?: false)
    }

    override fun saveBatch(list: List<EventWriteDto>): List<EventShortDto> {
        return eventService.saveBatch(list)
    }
}