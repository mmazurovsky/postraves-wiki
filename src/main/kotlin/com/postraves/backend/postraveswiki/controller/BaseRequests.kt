package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.BaseDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/default")
interface BaseRequests<DTO : BaseDto> {

    @GetMapping("/public/{id}")
    fun findById(@PathVariable id: Long): DTO
}
