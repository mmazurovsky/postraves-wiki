package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.repo.MyUserProfileRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import org.springframework.stereotype.Service

interface MyUserProfileService {
    fun findMyProfile(): UserFullDto?
    fun save(dto: UserWriteDto): UserShortDto
    fun update(dto: UserWriteDto)
    fun deleteMyProfile()
    fun followArtist(id: Long)
    fun unfollowArtist(id: Long)
    fun findMyFollowsArtist(): List<ArtistShortDto>
    fun findByAuthUidForSecurityService(authUid: String): UserFullDto?
}

@Service
class MyUserProfileServiceImpl(
    private val myUserProfileRepo: MyUserProfileRepo,
    private val securityService: SecurityService,
    private val artistService: ArtistService,
) : MyUserProfileService {

    override fun findMyProfile(): UserFullDto? {
        return myUserProfileRepo.findMyProfile(securityService.userAuthUid ?: return null)
    }

    override fun deleteMyProfile() {
        myUserProfileRepo.deleteMyProfile(securityService.userAuthUid ?: return)
    }

    override fun followArtist(id: Long) {
        // todo check if no association already
        myUserProfileRepo.followArtist(securityService.userAuthUid ?: throw TODO(), id)
        artistService.incrementFollowers(id)
    }

    override fun unfollowArtist(id: Long) {
        // todo check if association exists already
        myUserProfileRepo.unfollowArtist(securityService.userAuthUid ?: throw TODO(), id)
        artistService.decrementFollowers(id)
    }

    override fun findMyFollowsArtist(): List<ArtistShortDto> {
        val myFollows = myUserProfileRepo.findMyFollowsArtist(securityService.userAuthUid ?: throw TODO())
        return myFollows.map {
            artistService.enrichWithFollowersCalculationRequired(it)
        }.toList()
    }

    override fun findByAuthUidForSecurityService(authUid: String): UserFullDto? {
        return myUserProfileRepo.findMyProfile(authUid)
    }

    override fun save(dto: UserWriteDto): UserShortDto {
        return myUserProfileRepo.save(dto, securityService.userAuthUid ?: throw TODO())
    }

    override fun update(dto: UserWriteDto) {
        myUserProfileRepo.update(dto, securityService.userAuthUid ?: throw TODO())
    }
}