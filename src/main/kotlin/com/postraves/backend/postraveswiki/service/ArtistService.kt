package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.ArtistRepo
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import jooq.tables.records.ArtistRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service


interface ArtistService : BaseMethodsService<ArtistRecord, ArtistWriteDto, ArtistShortDto, ArtistFullDto>

@Service
class ArtistServiceImpl(
    private val artistRepo: ArtistRepo,
    @Qualifier("baseMethodsServiceImpl")
    private val baseMethodsService:
    BaseMethodsService<ArtistRecord, ArtistWriteDto, ArtistShortDto, ArtistFullDto>
    = BaseMethodsServiceImpl(artistRepo)
    ) : ArtistService {

    override fun findById(id: Long): ArtistFullDto {
        return baseMethodsService.findById(id)
    }

    override fun save(dto: ArtistWriteDto): ArtistFullDto {
        return baseMethodsService.save(dto)
    }

    override fun update(dto: ArtistWriteDto): ArtistFullDto {
        return baseMethodsService.update(dto)
    }

    override fun deleteById(id: Long) {
        baseMethodsService.deleteById(id)
    }
}