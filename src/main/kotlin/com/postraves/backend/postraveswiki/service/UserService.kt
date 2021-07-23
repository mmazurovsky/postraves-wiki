package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.repo.QuickEntityCountryRepo
import com.postraves.backend.postraveswiki.repo.UserRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.generic.BaseService
import org.springframework.stereotype.Service

interface UserService : BaseService<UserWriteDto, UserShortDto> {
    fun findMyProfile(): UserFullDto?
    fun deleteMyProfile()
    fun followArtist(id: Long)
    fun unfollowArtist(id: Long)
    fun findMyFollowsArtist(): List<ArtistShortDto>
    fun findByAuthUidForSecurityService(authUid: String): UserFullDto?
}

@Service
class UserServiceImpl(
    private val userRepo: UserRepo,
    private val securityService: SecurityService,
    private val artistService: ArtistService,
    private val quickEntityCountryRepo: QuickEntityCountryRepo,
) : UserService {

    override fun findMyProfile(): UserFullDto? {
        return userRepo.findMyProfile(securityService.userAuthUid ?: return null)
    }

    override fun deleteMyProfile() {
        userRepo.deleteMyProfile(securityService.userAuthUid ?: return)
    }

    override fun followArtist(id: Long) {
        // todo check if no association already
        userRepo.followArtist(securityService.userAuthUid ?: throw TODO(), id)
        artistService.incrementFollowers(id)
    }

    override fun unfollowArtist(id: Long) {
        // todo check if association exists already
        userRepo.unfollowArtist(securityService.userAuthUid ?: throw TODO(), id)
        artistService.decrementFollowers(id)
    }

    override fun findMyFollowsArtist(): List<ArtistShortDto> {
        val myFollows = userRepo.findMyFollowsArtist(securityService.userAuthUid ?: throw TODO())
        return myFollows.map {
            artistService.enrichShortWithFollowers(it)
        }.toList()
    }

    override fun findByAuthUidForSecurityService(authUid: String): UserFullDto? {
        return userRepo.findMyProfile(authUid)
    }

    override fun save(dto: UserWriteDto): UserShortDto {
        return userRepo.save(dto, securityService.userAuthUid ?: throw TODO())
    }

    override fun update(dto: UserWriteDto) {
        userRepo.update(dto, securityService.userAuthUid ?: throw TODO())
    }

    override fun findAll(): List<UserShortDto> {
        TODO("Not yet implemented")
    }
}