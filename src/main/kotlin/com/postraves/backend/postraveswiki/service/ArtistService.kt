package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.repo.ArtistRepo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service


interface ArtistService :
    BaseService<ArtistWriteDto, ArtistShortDto, ArtistFullDto>,
    ByIdService<ArtistFullDto>,
    RatingService<ArtistShortDto>

@Service
class ArtistServiceImpl(
    private val artistRepo: ArtistRepo,
    @Qualifier("baseServiceImpl")
    private val baseService:
    BaseService<ArtistWriteDto, ArtistShortDto, ArtistFullDto> = BaseServiceImpl(artistRepo),
    @Qualifier("ratingServiceImpl")
    private val ratingService: RatingService<ArtistShortDto> = RatingServiceImpl(artistRepo),
    @Qualifier("byIdServiceImpl")
    private val byIdService: ByIdService<ArtistFullDto> = ByIdServiceImpl(artistRepo)
    ) : ArtistService {

    override fun findById(id: Long): ArtistFullDto {
        return byIdService.findById(id)
    }

    override fun deleteById(id: Long) : ArtistFullDto {
        return byIdService.deleteById(id)
    }

    override fun save(dto: ArtistWriteDto): ArtistFullDto {
        return baseService.save(dto)
    }

    override fun update(dto: ArtistWriteDto): ArtistFullDto {
        return baseService.update(dto)
    }

    override fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<ArtistShortDto> {
        return ratingService.findOverallTopInCountry(countryName, maxQuantity)
    }

    override fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<ArtistShortDto> {
        return ratingService.findWeeklyTopInCountry(countryName, maxQuantity)
    }

    override fun findOfTheWeekInCountry(countryName: String): ArtistShortDto {
        return ratingService.findOfTheWeekInCountry(countryName)
    }

    override fun changeBaseRating(id: Long, socialMediaFollowersCount: Int) {
        return ratingService.changeBaseRating(id, socialMediaFollowersCount)
    }

    override fun findAll(): List<ArtistShortDto> {
        return baseService.findAll()
    }
}