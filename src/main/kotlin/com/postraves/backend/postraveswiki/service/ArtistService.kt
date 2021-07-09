package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.CountryRepo
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.repo.ArtistRepo
import jooq.tables.records.ArtistRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service


interface ArtistService : BaseService<ArtistWriteDto, ArtistShortDto, ArtistFullDto>, BaseRatingService<ArtistFullDto>

@Service
class ArtistServiceImpl(
    private val artistRepo: ArtistRepo,
    @Qualifier("baseServiceImpl")
    private val baseService:
    BaseService<ArtistWriteDto, ArtistShortDto, ArtistFullDto> = BaseServiceImpl(artistRepo),
    @Qualifier("baseRatingServiceImpl")
    private val baseRatingService: BaseRatingService<ArtistFullDto> = BaseRatingServiceImpl(artistRepo)
    ) : ArtistService {

    override fun findById(id: Long): ArtistFullDto {
        return baseRatingService.findById(id)
    }

    override fun save(dto: ArtistWriteDto): ArtistFullDto {
        return baseService.save(dto)
    }

    override fun update(dto: ArtistWriteDto): ArtistFullDto {
        return baseService.update(dto)
    }

    override fun deleteById(id: Long) : ArtistFullDto {
        return baseRatingService.deleteById(id)
    }

    override fun findAll(): List<ArtistShortDto> {
        return baseService.findAll()
    }
}