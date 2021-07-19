package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/default")
interface ByNameRequests<FULLDTO : BaseFullDto> {

    @GetMapping("/public/{name}")
    @ResponseStatus(HttpStatus.OK)
    fun findByName(@PathVariable name: String): FULLDTO

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteByName(@PathVariable name: String)
}
