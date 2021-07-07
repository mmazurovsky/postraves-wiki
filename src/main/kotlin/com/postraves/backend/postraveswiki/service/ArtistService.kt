package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.ArtistRepo
import com.postraves.backend.postraveswiki.data.dto.ArtistDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service


interface ArtistService : BaseMethodsService<ArtistDto> {
    override fun findById(id: Long) : ArtistDto
    override fun findAll(): List<ArtistDto>
    override fun save(dto: ArtistDto): ArtistDto
    override fun update(dto: ArtistDto): ArtistDto
    override fun deleteById(id: Long)
}

@Service
class ArtistServiceImpl(
    private val artistRepo: ArtistRepo,
    @Qualifier("baseMethodsServiceImpl")
    private val baseMethodsService: BaseMethodsService<ArtistDto> = BaseMethodsServiceImpl(artistRepo)
    ) : ArtistService {

    override fun findById(id: Long): ArtistDto {
        return baseMethodsService.findById(id)
    }

    override fun findAll(): List<ArtistDto> {
        return baseMethodsService.findAll()
    }

    override fun save(dto: ArtistDto): ArtistDto {
        return baseMethodsService.save(dto)
    }

    override fun update(dto: ArtistDto): ArtistDto {
        return baseMethodsService.update(dto)
    }

    override fun deleteById(id: Long) {
        baseMethodsService.deleteById(id)
    }
}