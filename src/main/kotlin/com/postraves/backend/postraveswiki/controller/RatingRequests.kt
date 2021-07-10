package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/default")
interface RatingRequests<SHORTDTO : BaseShortDto, FULLDTO : BaseFullDto> {

    @GetMapping("/public/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findById(@PathVariable id: Long): FULLDTO

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteById(@PathVariable id: Long): FULLDTO

    @GetMapping("/public/overallRating")
    @ResponseStatus(HttpStatus.OK)
    fun findOverallRating(@RequestParam cityName: String, @RequestParam maxQuantity: Int) : List<SHORTDTO>
}
