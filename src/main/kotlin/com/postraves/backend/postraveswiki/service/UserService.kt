package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.repo.UserRepo
import com.postraves.backend.postraveswiki.repo.WeeklyFollowersDeltaRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.generic.BaseService
import org.springframework.stereotype.Service

interface UserService : BaseService<UserWriteDto, UserShortDto> {
    fun findMyProfile() : UserFullDto?
    fun deleteMyProfile()
    fun followArtist(id: Long)
    fun unfollowArtist(id: Long)
    fun findMyFollowsArtist() : List<ArtistShortDto>
    fun findByAuthUidForSecurityService(authUid: String) : UserFullDto?
    fun checkIsFollowedArtist(id: Long) : Boolean
}

@Service
class UserServiceImpl(
    private val userRepo: UserRepo,
    private val securityService: SecurityService,
    private val artistService: ArtistService,
    private val weeklyFollowersDeltaRepo: WeeklyFollowersDeltaRepo,
    ) : UserService {

    override fun findMyProfile(): UserFullDto? {
        return if (securityService.userAuthUid != null)
            userRepo.findMyProfile(securityService.userAuthUid ?: throw TODO())
        else null
    }

    override fun deleteMyProfile() {
        userRepo.deleteMyProfile(securityService.userAuthUid ?: throw TODO())
    }

    override fun followArtist(id: Long) {
        // todo check if no association already
        val entityType = "artist"
        userRepo.followArtist(securityService.userAuthUid ?: throw TODO(), id)
        // todo refactor it
        val countryName = artistService.findById(id).country?.name
        artistService.incrementOverallFollowers(id)
        if (countryName != null)
            weeklyFollowersDeltaRepo.incrementWeeklyFollowersDelta(entityType, countryName, id)
    }

    override fun unfollowArtist(id: Long) {
        // todo check if association exists already
        val entityType = "artist"
        userRepo.unfollowArtist(securityService.userAuthUid ?: throw TODO(), id)
        // todo refactor it
        val countryName = artistService.findById(id).country?.name
        artistService.decrementOverallFollowers(id)
        if (countryName != null)
            weeklyFollowersDeltaRepo.incrementWeeklyFollowersDelta(entityType, countryName, id)
    }

    override fun findMyFollowsArtist(): List<ArtistShortDto> {
        return userRepo.findMyFollowsArtist(securityService.userAuthUid ?: throw TODO())
    }

    override fun findByAuthUidForSecurityService(authUid: String): UserFullDto? {
        return userRepo.findMyProfile(authUid)
    }

    override fun checkIsFollowedArtist(id: Long): Boolean {
        return if (findMyProfile() != null)
            userRepo.checkIsFollowedArtist(securityService.userAuthUid ?: throw TODO(), id)
        else false
    }

    override fun save(dto: UserWriteDto):UserShortDto {
        return userRepo.save(dto, securityService.userAuthUid ?: throw TODO())
    }

   override fun update(dto: UserWriteDto) {
       // get auth id
       // pass it with dto
       userRepo.update(dto, securityService.userAuthUid ?: throw TODO())
    }

    override fun findAll(): List<UserShortDto> {
        TODO("Not yet implemented")
    }
}