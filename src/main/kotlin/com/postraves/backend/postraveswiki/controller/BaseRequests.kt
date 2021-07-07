package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/default")
interface BaseRequests<SHORTDTO : BaseShortDto, FULLDTO : BaseFullDto> {

    @PostMapping
    fun save(@RequestBody dto: FULLDTO): ResponseEntity<FULLDTO>

    @PutMapping
    fun update(@RequestBody dto: FULLDTO): ResponseEntity<FULLDTO>

    @GetMapping("/public/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<FULLDTO>

    @GetMapping
    fun findAll(): ResponseEntity<List<SHORTDTO>>

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: Long): ResponseEntity<FULLDTO>
}
