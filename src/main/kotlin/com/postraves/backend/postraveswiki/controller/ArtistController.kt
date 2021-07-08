package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.service.ArtistService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/artist")
class ArtistController(private val artistService: ArtistService) : BaseRequests<ArtistShortDto, ArtistFullDto> {

    override fun save(dto: ArtistFullDto): ResponseEntity<ArtistFullDto> {
        TODO("Not yet implemented")
    }

    override fun update(dto: ArtistFullDto): ResponseEntity<ArtistFullDto> {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): ResponseEntity<ArtistFullDto> {
        return ResponseEntity.ok(artistService.findById(id))
    }

    override fun findAll(): ResponseEntity<List<ArtistShortDto>> {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long): ResponseEntity<ArtistFullDto> {
        TODO("Not yet implemented")
    }
}