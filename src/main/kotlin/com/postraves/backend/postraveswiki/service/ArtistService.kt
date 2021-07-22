package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.repo.*
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.generic.BaseService
import com.postraves.backend.postraveswiki.service.generic.ByIdService
import com.postraves.backend.postraveswiki.service.generic.RatingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


interface ArtistService :
    BaseService<ArtistWriteDto, ArtistShortDto>,
    ByIdService<ArtistFullDto, ArtistShortDto>,
    RatingService<ArtistShortDto>

@Service
class ArtistServiceImpl(
    private val artistRepo: ArtistRepo,
    private val securityService: SecurityService,
    private val artistCountryRepoImpl: QuickEntityCountryRepoImpl.ArtistCountryQuickRepoImpl,
    private val weeklyBestRepo: WeeklyBestRepo,
    private val artistWeeklyFollowersDeltaImpl: QuickFollowersRepoImpl.ArtistWeeklyQuickFollowersDeltaRepoImpl,
    private val artistOverallFollowersImpl: QuickFollowersRepoImpl.ArtistOverallQuickFollowersRepoImpl,
) : ArtistService {

    @Autowired
    private lateinit var userService: UserService

    private fun findByIdDependingOnUser(id: Long): ArtistFullDto {
        val user = userService.findMyProfile()
        return if (user == null)
            artistRepo.findById(id)
        else
            artistRepo.findByIdForUser(securityService.userAuthUid!!, id)
    }

    private fun enrichWithFollowers(artist: ArtistFullDto): ArtistFullDto {
        val weeklyFollowers = artistWeeklyFollowersDeltaImpl.getFollowers(artist.id)
        val overallFollowers = artistOverallFollowersImpl.getFollowers(artist.id)
        return artist.copy(weeklyFollowers = weeklyFollowers, overallFollowers = overallFollowers)
    }

    private fun findListByIds(ids: Set<Long>): List<ArtistShortDto> {
        return artistRepo.findListByIds(ids)
    }

    override fun findById(id: Long): ArtistFullDto {
        val foundArtist = findByIdDependingOnUser(id)
        return enrichWithFollowers(foundArtist)
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
        val artistFromTheCountryIds = artistCountryRepoImpl.getAllIdsByCountry(countryName)
        val topArtistIdsAndScores = artistOverallFollowersImpl.findTop(-1)
        val topArtistFromTheCountryIdsAndScores = topArtistIdsAndScores.filterKeys { artistFromTheCountryIds.contains(it) }.toMap()
        // todo maybe order gets lost here
        val topArtistFromTheCountryIds = topArtistFromTheCountryIdsAndScores.keys
        val topArtistFromTheCountryDtos = findListByIds(topArtistFromTheCountryIds)
        val topArtistFromTheCountryDtosWithOverallFollowers = topArtistFromTheCountryDtos.map {
            it.copy(overallFollowers = topArtistFromTheCountryIdsAndScores[it.id] ?: TODO())
        }.toList()
//        Collections.sort(topArtistsFromTheCountryDtos, Comparator.comparing { topArtistsFromTheCountryIds.indexOf(it.id) })
        return topArtistFromTheCountryDtosWithOverallFollowers.subList(0, maxQuantity)
    }

    // todo rewrite with getting from redis
    override fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<ArtistShortDto> {
        val artistFromTheCountryIds = artistCountryRepoImpl.getAllIdsByCountry(countryName)
        val weeklyTopArtistIdsAndScores = artistWeeklyFollowersDeltaImpl.findTop(-1)
        val weeklyTopArtistFromTheCountryIdsAndScores = weeklyTopArtistIdsAndScores.filterKeys { artistFromTheCountryIds.contains(it) }.toMap()
        // todo maybe order gets lost here
        val weeklyTopArtistFromTheCountryIds = weeklyTopArtistIdsAndScores.keys
        val weeklyTopArtistFromTheCountryDtos = findListByIds(weeklyTopArtistFromTheCountryIds)
        val weeklyTopArtistDtosWithWeeklyFollowers = weeklyTopArtistFromTheCountryDtos.map {
            it.copy(weeklyFollowers = weeklyTopArtistFromTheCountryIdsAndScores[it.id] ?: TODO())
        }.toList()
//        Collections.sort(weeklyTopArtistFromTheCountryDtos, Comparator.comparing { weeklyTopArtistFromTheCountryIds.indexOf(it.id) })
        return weeklyTopArtistDtosWithWeeklyFollowers.subList(0, maxQuantity)
    }

    override fun findBestOfTheWeekInCountry(countryName: String): ArtistShortDto {
        TODO()
    }

    override fun changeBaseRating(id: Long, socialMediaFollowersCount: Int) {

    }

    // todo rewrite with redis
    override fun incrementFollowers(id: Long) {
        if (securityService.userAuthUid != null) {
            artistOverallFollowersImpl.incrementFollowers(id)
            artistWeeklyFollowersDeltaImpl.incrementFollowers(id)
        }
    }

    // todo rewrite with redis
    override fun decrementFollowers(id: Long) {
        if (securityService.userAuthUid != null) {
            artistOverallFollowersImpl.decrementFollowers(id)
            artistWeeklyFollowersDeltaImpl.decrementFollowers(id)
        }
    }

    override fun findAll(): List<ArtistShortDto> {
        return artistRepo.findAll()
    }
}