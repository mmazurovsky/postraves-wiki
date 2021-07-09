package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.service.ArtistService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/artist")
class ArtistController(private val artistService: ArtistService) : BaseRequests<ArtistWriteDto, ArtistShortDto, ArtistFullDto> {

    override fun save(dto: ArtistWriteDto): ResponseEntity<ArtistFullDto> {
        return ResponseEntity.ok(artistService.save(dto))
    }

    override fun update(dto: ArtistWriteDto): ResponseEntity<ArtistFullDto> {
        TODO("Not yet implemented")
    }

    @GetMapping("/public/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<ArtistFullDto> {
        return ResponseEntity.ok(artistService.findById(id))
    }

    override fun findAll(): ResponseEntity<List<ArtistShortDto>> {
        TODO("Not yet implemented")
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: Long): ResponseEntity<ArtistFullDto> {
        TODO("Not yet implemented")
    }
}