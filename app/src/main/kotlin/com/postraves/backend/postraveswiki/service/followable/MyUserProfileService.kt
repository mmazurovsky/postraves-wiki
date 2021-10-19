package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.exception.FollowingException
import com.postraves.backend.postraveswiki.exception.NotAuthenticated
import com.postraves.backend.postraveswiki.repo.followable.MyUserProfileRepo
import com.postraves.backend.postraveswiki.security.SecurityService
import org.springframework.stereotype.Service

interface MyUserProfileService {
    fun getMyUserId(): Long?
    fun save(dto: UserWriteDto): UserShortDto
    fun update(dto: UserWriteDto)
    fun deleteMyProfile()
    fun followArtist(id: Long)
    fun unfollowArtist(id: Long)
    fun followEvent(id: Long)
    fun unfollowEvent(id: Long)
    fun followPlace(id: Long)
    fun unfollowPlace(id: Long)
    fun followUnity(id: Long)
    fun unfollowUnity(id: Long)
    fun findMyFollowingArtists(): List<ArtistShortDto>
    fun findMyFollowingEvents(): List<EventShortDto>
    fun findMyFollowingUnities(): List<UnityShortDto>
    fun findMyFollowingPlaces(): List<PlaceShortDto>
    fun findByAuthUidForSecurityService(authUid: String): UserFullDto?
    fun checkNicknameIsFree(nickname: String): Boolean
}

@Service
class MyUserProfileServiceImpl(
    private val myUserProfileRepo: MyUserProfileRepo,
    private val securityService: SecurityService,
    private val artistService: ArtistService,
    private val eventService: EventService,
    private val placeService: PlaceService,
    private val unityService: UnityService,
) : MyUserProfileService {

    override fun getMyUserId(): Long? {
        return securityService.user?.id
    }

    override fun deleteMyProfile() {
        val userAuthUid = securityService.firebaseAuthUid
        if (userAuthUid != null)
            myUserProfileRepo.deleteMyProfile(userAuthUid)
    }

    override fun checkNicknameIsFree(nickname: String): Boolean {
        return myUserProfileRepo.checkNicknameIsFree(nickname)
    }

    override fun followArtist(id: Long) {
        val userId = getMyUserId()
        if (userId != null)
            if (!myUserProfileRepo.checkArtistIsFollowed(id, userId)) {
                myUserProfileRepo.followArtist(userId, id)
                artistService.incrementFollowers(id)
            } else {
                logger.info("Trying to follow one same artist multiple times user: $userId, artist: $id")
                throw FollowingException(
                    userId = userId,
                    entity = "Artist",
                    entityId = id.toString(),
                    message = "already followed"
                )
            }
    }

    override fun unfollowArtist(id: Long) {
        val userId = getMyUserId()
        if (userId != null)
            if (myUserProfileRepo.checkArtistIsFollowed(id, userId)) {
                myUserProfileRepo.unfollowArtist(userId, id)
                artistService.decrementFollowers(id)
            } else {
                logger.info("Trying to unfollow one same artist multiple times user: $userId, artist: $id")
                throw FollowingException(
                    userId = userId,
                    entity = "Artist",
                    entityId = id.toString(),
                    message = "already unfollowed"
                )
            }
    }

    override fun followEvent(id: Long) {
        val authUid = getMyUserId()
        if (authUid != null)
            if (!myUserProfileRepo.checkEventIsFollowed(id, authUid)) {
                myUserProfileRepo.followEvent(authUid, id)
                eventService.incrementFollowers(id)
            } else {
                logger.info("Trying to follow one same artist multiple times user: $authUid, artist: $id")
//                throw TODO()
            }
    }

    override fun unfollowEvent(id: Long) {
        val authUid = getMyUserId()
        if (authUid != null)
            if (myUserProfileRepo.checkEventIsFollowed(id, authUid)) {
                myUserProfileRepo.unfollowEvent(authUid, id)
                eventService.decrementFollowers(id)
            } else {
                logger.info("Trying to unfollow one same artist multiple times user: $authUid, artist: $id")
            }
    }

    override fun followPlace(id: Long) {
        val authUid = getMyUserId()
        if (authUid != null)
            if (!myUserProfileRepo.checkPlaceIsFollowed(id, authUid)) {
                myUserProfileRepo.followPlace(authUid, id)
                placeService.incrementFollowers(id)
            } else {
                logger.info("Trying to follow one same artist multiple times user: $authUid, artist: $id")
//                throw TODO()
            }
    }

    override fun unfollowPlace(id: Long) {
        val authUid = getMyUserId()
        if (authUid != null)
            if (myUserProfileRepo.checkPlaceIsFollowed(id, authUid)) {
                myUserProfileRepo.unfollowPlace(authUid, id)
                placeService.decrementFollowers(id)
            } else {
                logger.info("Trying to unfollow one same artist multiple times user: $authUid, artist: $id")
            }
    }

    override fun followUnity(id: Long) {
        val authUid = getMyUserId()
        if (authUid != null)
            if (!myUserProfileRepo.checkUnityIsFollowed(id, authUid)) {
                myUserProfileRepo.followUnity(authUid, id)
                unityService.incrementFollowers(id)
            } else {
                logger.info("Trying to follow one same artist multiple times user: $authUid, artist: $id")
//                throw TODO()
            }
    }

    override fun unfollowUnity(id: Long) {
        val authUid = getMyUserId()
        if (authUid != null)
            if (myUserProfileRepo.checkUnityIsFollowed(id, authUid)) {
                myUserProfileRepo.unfollowUnity(authUid, id)
                unityService.decrementFollowers(id)
            } else {
                logger.info("Trying to unfollow one same artist multiple times user: $authUid, artist: $id")
            }
    }

    override fun findMyFollowingArtists(): List<ArtistShortDto> {
        val authUid = getMyUserId()
        return if (authUid != null) {
            val myFollows =
                myUserProfileRepo.findMyFollowingArtists(authUid)
            myFollows.map {
                artistService.enrichWithFollowersCalculationRequired(it)
            }.toList()
        } else throw NotAuthenticated()
    }

    override fun findMyFollowingEvents(): List<EventShortDto> {
        val userId = getMyUserId()
        return if (userId != null) {
            val myFollows =
                myUserProfileRepo.findMyFollowingEvents(userId)
            myFollows.map {
                eventService.enrichWithFollowersCalculationRequired(it)
            }.toList()
        } else throw NotAuthenticated()
    }

    override fun findMyFollowingUnities(): List<UnityShortDto> {
        val authUid = getMyUserId()
        return if (authUid != null) {
            val myFollows =
                myUserProfileRepo.findMyFollowingUnities(authUid)
            myFollows.map {
                unityService.enrichWithFollowersCalculationRequired(it)
            }.toList()
        } else throw NotAuthenticated()
    }

    override fun findMyFollowingPlaces(): List<PlaceShortDto> {
        val authUid = getMyUserId()
        return if (authUid != null) {
            val myFollows =
                myUserProfileRepo.findMyFollowingPlaces(authUid)
            myFollows.map {
                placeService.enrichWithFollowersCalculationRequired(it)
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
        val userId = getMyUserId() ?: throw NotAuthenticated()
        myUserProfileRepo.update(dto, userId)
    }
}
