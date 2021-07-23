package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("/default")
interface RatingRequests<SHORTDTO : BaseShortDto> {

    @GetMapping("/public/overallRating")
    @ResponseStatus(HttpStatus.OK)
    fun findOverallRatingForCityByCountry(@RequestParam cityName: String, @RequestParam maxQuantity: Int): List<SHORTDTO>
}
