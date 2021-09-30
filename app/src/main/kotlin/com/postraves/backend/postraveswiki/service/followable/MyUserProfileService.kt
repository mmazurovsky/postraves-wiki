package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.exception.NotAuthenticated
import com.postraves.backend.postraveswiki.repo.followable.MyUserProfileRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import org.springframework.stereotype.Service

interface MyUserProfileService {
//    fun getMyProfileInSecurityService(): UserFullDto?
//    fun getMyAuthUidInSecurityService(): String?
    fun getMyAuthUidOnlyIfUserProfileExists(): String?
    fun save(dto: UserWriteDto): UserShortDto
    fun update(dto: UserWriteDto)
    fun deleteMyProfile()
    fun followArtist(id: Long)
    fun unfollowArtist(id: Long)
    fun findMyFollowsArtist(): List<ArtistShortDto>
    fun findByAuthUidForSecurityService(authUid: String): UserFullDto?
    fun checkArtistIsFollowed(id: Long): Boolean
    fun checkNicknameIsFree(nickname: String): Boolean
}

@Service
class MyUserProfileServiceImpl(
    private val myUserProfileRepo: MyUserProfileRepo,
    private val securityService: SecurityService,
    private val artistService: ArtistService,
) : MyUserProfileService {

    override fun getMyAuthUidOnlyIfUserProfileExists(): String? {
        val myProfile = securityService.user
        val myAuthUid = securityService.firebaseAuthUid
        return if (myProfile == null) null
        else myAuthUid
    }

    override fun deleteMyProfile() {
        val authUid = getMyAuthUidOnlyIfUserProfileExists()
        if (authUid != null)
            myUserProfileRepo.deleteMyProfile(authUid)
    }

    override fun checkArtistIsFollowed(id: Long): Boolean {
        val authUid = getMyAuthUidOnlyIfUserProfileExists()
        return myUserProfileRepo.checkArtistIsFollowed(id, authUid ?: throw NotAuthenticated())
    }

    override fun checkNicknameIsFree(nickname: String): Boolean {
        return myUserProfileRepo.checkNicknameIsFree(nickname)
    }

    override fun followArtist(id: Long) {
        val authUid = getMyAuthUidOnlyIfUserProfileExists()
        if (authUid != null)
            if (!checkArtistIsFollowed(id)) {
                myUserProfileRepo.followArtist(authUid, id)
                artistService.incrementFollowers(id)
            } else {
                logger.info("Trying to follow one same artist multiple times user: $authUid, artist: $id")
//                throw TODO()
            }
    }

    override fun unfollowArtist(id: Long) {
        val authUid = getMyAuthUidOnlyIfUserProfileExists()
        if (authUid != null)
            if (checkArtistIsFollowed(id)) {
                myUserProfileRepo.unfollowArtist(authUid, id)
                artistService.decrementFollowers(id)
            } else {
                logger.info("Trying to unfollow one same artist multiple times user: $authUid, artist: $id")
            }
    }

    override fun findMyFollowsArtist(): List<ArtistShortDto> {
        val authUid = getMyAuthUidOnlyIfUserProfileExists()
        return if (authUid != null) {
            val myFollows =
                myUserProfileRepo.findMyFollowsArtist(authUid)
            myFollows.map {
                artistService.enrichWithFollowersCalculationRequired(it)
            }.toList()
        } else throw NotAuthenticated()
    }

    override fun findByAuthUidForSecurityService(authUid: String): UserFullDto? {
        return myUserProfileRepo.findMyProfileByAuthUid(authUid)
    }

    override fun save(dto: UserWriteDto): UserShortDto {
        val authUid = securityService.firebaseAuthUid
        return myUserProfileRepo.save(dto, authUid ?: throw NotAuthenticated())
    }

    override fun update(dto: UserWriteDto) {
        val authUid = getMyAuthUidOnlyIfUserProfileExists() ?: throw NotAuthenticated()
        myUserProfileRepo.update(dto, authUid)
    }
}
