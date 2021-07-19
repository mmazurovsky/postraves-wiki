package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.repo.ArtistRepo
import com.postraves.backend.postraveswiki.service.generic.*
import org.springframework.stereotype.Service

interface ArtistService :
    BaseService<ArtistWriteDto, ArtistShortDto>,
    ByIdService<ArtistFullDto>,
    RatingService<ArtistShortDto>

@Service
class ArtistServiceImpl(
    private val artistRepo: ArtistRepo,
) : ArtistService {

    override fun findById(id: Long): ArtistFullDto {
        return artistRepo.findById(id)
    }

    override fun deleteById(id: Long) {
        artistRepo.deleteById(id)
    }

    override fun save(dto: ArtistWriteDto): ArtistShortDto {
        return artistRepo.save(dto)
    }

    override fun update(dto: ArtistWriteDto) {
        artistRepo.update(dto)
    }

    override fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<ArtistShortDto> {
        return artistRepo.findOverallTopInCountry(countryName, maxQuantity)
    }

    override fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<ArtistShortDto> {
        return artistRepo.findWeeklyTopInCountry(countryName, maxQuantity)
    }

    override fun findOfTheWeekInCountry(countryName: String): ArtistShortDto {
        return artistRepo.findOfTheWeekInCountry(countryName)
    }

    override fun changeBaseRating(id: Long, socialMediaFollowersCount: Int) {
        artistRepo.changeBaseRating(id, socialMediaFollowersCount)
    }

    override fun findAll(): List<ArtistShortDto> {
        return artistRepo.findAll()
    }
}