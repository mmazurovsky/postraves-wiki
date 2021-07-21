package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.repo.ArtistRepo
import com.postraves.backend.postraveswiki.repo.WeeklyBestRepo
import com.postraves.backend.postraveswiki.repo.WeeklyFollowersDeltaRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.generic.BaseService
import com.postraves.backend.postraveswiki.service.generic.ByIdService
import com.postraves.backend.postraveswiki.service.generic.RatingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*


interface ArtistService :
    BaseService<ArtistWriteDto, ArtistShortDto>,
    ByIdService<ArtistFullDto, ArtistShortDto>,
    RatingService<ArtistShortDto>

@Service
class ArtistServiceImpl(
    private val artistRepo: ArtistRepo,
    private val securityService: SecurityService,
    private val weeklyBestRepo: WeeklyBestRepo,
    private val weeklyFollowersDeltaRepo: WeeklyFollowersDeltaRepo,
) : ArtistService {

    @Autowired
    private lateinit var userService: UserService

    private val entityType: String = "artist"

    override fun findById(id: Long): ArtistFullDto {
        val user = userService.findMyProfile()
        val foundArtist = if (user == null) artistRepo.findById(id)
        else artistRepo.findByIdForUser(securityService.userAuthUid!!, id)
        // todo encaps it
        val weeklyFollowersDelta = if (foundArtist.country != null)
            weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, foundArtist.country.name, foundArtist.id)
        else null
        foundArtist.weeklyFollowersDelta = weeklyFollowersDelta
        return foundArtist
    }

    override fun deleteById(id: Long) {
        artistRepo.deleteById(id)
    }

    override fun findListByIds(ids: Set<Long>): List<ArtistShortDto> {
        return artistRepo.findListByIds(ids.toSet())
    }

    override fun save(dto: ArtistWriteDto): ArtistShortDto {
        return artistRepo.save(dto)
    }

    override fun update(dto: ArtistWriteDto) {
        artistRepo.update(dto)
    }

    override fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<ArtistShortDto> {
        // todo rewrite get from redis
        return artistRepo.findOverallTopInCountry(countryName, maxQuantity)
    }

    // todo rewrite with getting from redis
    override fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<ArtistShortDto> {
        val topMap = weeklyFollowersDeltaRepo.findWeeklyTopInCountry(entityType, countryName, 50)
        // todo maybe order gets lost here
        val ids = topMap.keys
        val topArtists = findListByIds(ids)
        Collections.sort(topArtists, Comparator.comparing { ids.indexOf(it.id) })
        topArtists.forEach { it.weeklyFollowersDelta = topMap[it.id] }
        return topArtists
    }

    override fun findBestOfTheWeekInCountry(countryName: String): ArtistShortDto {
        TODO()
    }

    override fun changeBaseRating(id: Long, socialMediaFollowersCount: Int) {
        artistRepo.changeBaseRating(id, socialMediaFollowersCount)
    }

    // todo rewrite with redis
    override fun incrementOverallFollowers(id: Long) {
//        if (securityService.userAuthUid != null)
            artistRepo.incrementOverallFollowers(id)
    }

    // todo rewrite with redis
    override fun decrementOverallFollowers(id: Long) {
//        if (securityService.userAuthUid != null)
            artistRepo.decrementOverallFollowers(id)
    }

    override fun findAll(): List<ArtistShortDto> {
        return artistRepo.findAll()
    }
}