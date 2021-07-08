package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.ArtistRepo
import com.postraves.backend.postraveswiki.data.dto.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.ArtistShortDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service


interface ArtistService : BaseMethodsService<ArtistShortDto, ArtistFullDto>

@Service
class ArtistServiceImpl(
    private val artistRepo: ArtistRepo,
    @Qualifier("baseMethodsServiceImpl")
    private val baseMethodsService: BaseMethodsService<ArtistShortDto, ArtistFullDto> = BaseMethodsServiceImpl(artistRepo)
    ) : ArtistService {

    override fun findById(id: Long): ArtistFullDto {
        return baseMethodsService.findById(id)
    }

    override fun findAll(): List<ArtistShortDto> {
        return baseMethodsService.findAll()
    }

    override fun save(dto: ArtistFullDto): ArtistFullDto {
        return baseMethodsService.save(dto)
    }

    override fun update(dto: ArtistFullDto): ArtistFullDto {
        return baseMethodsService.update(dto)
    }

    override fun deleteById(id: Long) {
        baseMethodsService.deleteById(id)
    }
}