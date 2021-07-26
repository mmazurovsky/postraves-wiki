package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.exception.NotAuthenticated
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
    fun checkArtistIsFollowed(id: Long): Boolean
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
        if (findMyProfile() != null)
            myUserProfileRepo.deleteMyProfile(securityService.userAuthUid ?: return)
    }

    override fun checkArtistIsFollowed(id: Long): Boolean {
        return myUserProfileRepo.checkArtistIsFollowed(id, securityService.userAuthUid ?: throw NotAuthenticated())
    }

    override fun followArtist(id: Long) {
        if (findMyProfile() != null)
            if (!checkArtistIsFollowed(id)) {
                myUserProfileRepo.followArtist(securityService.userAuthUid ?: throw NotAuthenticated(), id)
                artistService.incrementFollowers(id)
            } else {
                logger.info("Trying to follow one same artist multiple times user: ${securityService.userAuthUid}, artist: $id")
            }
    }

    override fun unfollowArtist(id: Long) {
        if (findMyProfile() != null)
            if (checkArtistIsFollowed(id)) {
                myUserProfileRepo.unfollowArtist(securityService.userAuthUid ?: throw NotAuthenticated(), id)
                artistService.decrementFollowers(id)
            } else {
                logger.info("Trying to unfollow one same artist multiple times user: ${securityService.userAuthUid}, artist: $id")
            }
    }

    override fun findMyFollowsArtist(): List<ArtistShortDto> {
        return if (findMyProfile() != null) {
            val myFollows =
                myUserProfileRepo.findMyFollowsArtist(securityService.userAuthUid ?: throw NotAuthenticated())
            myFollows.map {
                artistService.enrichWithFollowersCalculationRequired(it)
            }.toList()
        } else emptyList()
    }

    override fun findByAuthUidForSecurityService(authUid: String): UserFullDto? {
        return myUserProfileRepo.findMyProfile(authUid)
    }

    override fun save(dto: UserWriteDto): UserShortDto {
        return myUserProfileRepo.save(dto, securityService.userAuthUid ?: throw NotAuthenticated())
    }

    override fun update(dto: UserWriteDto) {
        if (findMyProfile() != null)
            myUserProfileRepo.update(dto, securityService.userAuthUid ?: throw NotAuthenticated())
    }
}