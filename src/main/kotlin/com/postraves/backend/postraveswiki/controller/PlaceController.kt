package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.PlaceFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.PlaceShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.SceneDto
import com.postraves.backend.postraveswiki.data.dto.writing.PlaceWriteDto
import com.postraves.backend.postraveswiki.service.PlaceService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/place")
class PlaceController (
    private val placeService: PlaceService
    ) :
    BaseRequests<PlaceWriteDto, PlaceShortDto>,
    ByIdRequests<PlaceFullDto>,
    RatingRequests<PlaceShortDto>,
    FindByNameRequests<PlaceShortDto> {

    override fun save(dto: PlaceWriteDto): PlaceShortDto {
        return placeService.save(dto)
    }

    override fun update(dto: PlaceWriteDto) {
        placeService.update(dto)
    }

    override fun findById(id: Long): PlaceFullDto {
        return placeService.findById(id)
    }

    override fun findAll(): List<PlaceShortDto> {
        return placeService.findAll()
    }

    override fun deleteById(id: Long) {
        placeService.deleteById(id)
    }

    override fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<PlaceShortDto> {
        return placeService.findOverallRatingForCityByCountry(cityName, maxQuantity)
    }

    override fun findByPartOfName(namePart: String): List<PlaceShortDto> {
        return placeService.findByPartOfName(namePart)
    }

    @PutMapping("/{id}/scenes")
    @ResponseStatus(HttpStatus.OK)
    fun updateScenesOfPlace(@PathVariable id: Long, @RequestBody scenes: List<SceneDto>) {
        placeService.updateScenesOfPlace(id, scenes)
    }

    @GetMapping("/{id}/scenes")
    @ResponseStatus(HttpStatus.OK)
    fun getScenesOfPlace(@PathVariable id: Long): List<SceneDto> {
        return placeService.getScenesOfPlace(id)
    }
}