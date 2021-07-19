package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
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
