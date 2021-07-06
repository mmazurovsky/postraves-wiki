package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.ArtistDto
import com.postraves.backend.postraveswiki.data.dto.ArtistShortDto
import com.postraves.backend.postraveswiki.service.ArtistService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/artist")
class ArtistController(private val artistService: ArtistService) : BaseRequests<ArtistDto> {

    override fun findById(id: Long): ArtistDto {
        return artistService.findById(id)
    }
}