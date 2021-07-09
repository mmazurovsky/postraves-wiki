package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/default")
interface BaseRequests<WRITEDTO : BaseWriteDto, SHORTDTO : BaseShortDto, FULLDTO : BaseFullDto> {

    @PostMapping
    fun save(@RequestBody dto: WRITEDTO): ResponseEntity<FULLDTO>

    @PutMapping
    fun update(@RequestBody dto: WRITEDTO): ResponseEntity<FULLDTO>

    @GetMapping
    fun findAll(): ResponseEntity<List<SHORTDTO>>
}
