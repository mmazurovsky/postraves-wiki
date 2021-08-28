package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.data.dto.reading.EventShortDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/default")
interface BaseRequests<WRITEDTO : BaseWriteDto, SHORTDTO : BaseShortDto> {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun save(@RequestBody dto: WRITEDTO): SHORTDTO

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    fun update(@RequestBody dto: WRITEDTO)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun findAll(): List<SHORTDTO>
}

@RequestMapping("/default")
interface ByIdRequests<FULLDTO : BaseFullDto> {

    @GetMapping("/public/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findById(@PathVariable id: Long): FULLDTO

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteById(@PathVariable id: Long)
}

@RequestMapping("/default")
interface ByNameRequests<FULLDTO : BaseFullDto> {

    @GetMapping("/public/{name}")
    @ResponseStatus(HttpStatus.OK)
    fun findByName(@PathVariable name: String): FULLDTO

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteByName(@PathVariable name: String)
}

@RequestMapping("/default")
interface RatingRequests<SHORTDTO : BaseShortDto> {

    @GetMapping("/public/overallRating")
    @ResponseStatus(HttpStatus.OK)
    fun findOverallRatingForCityByCountry(@RequestParam cityName: String, @RequestParam maxQuantity: Int): List<SHORTDTO>

    @GetMapping("/public/weeklyRating")
    @ResponseStatus(HttpStatus.OK)
    fun findWeeklyRatingForCityByCountry(@RequestParam cityName: String, @RequestParam maxQuantity: Int): List<SHORTDTO>
}

@RequestMapping("/default")
interface FindByNameRequests<SHORTDTO : BaseShortDto> {

    @GetMapping("/public/search/{namePart}")
    @ResponseStatus(HttpStatus.OK)
    fun findByPartOfName(@PathVariable namePart: String): List<SHORTDTO>
}

@RequestMapping("/default")
interface RelevantEventsRequests {

    @GetMapping("/public/{id}/events")
    @ResponseStatus(HttpStatus.OK)
    fun getRelevantEvents(@PathVariable id: Long): List<EventShortDto>
}

