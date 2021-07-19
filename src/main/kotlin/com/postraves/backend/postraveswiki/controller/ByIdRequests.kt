package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/default")
interface ByIdRequests<FULLDTO : BaseFullDto> {

    @GetMapping("/public/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findById(@PathVariable id: Long): FULLDTO

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteById(@PathVariable id: Long)
}
