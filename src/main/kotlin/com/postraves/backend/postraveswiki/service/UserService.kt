package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.repo.UserRepo
import com.postraves.backend.postraveswiki.repo.QuickEntityCountryRepo
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
}

@Service
class UserServiceImpl(
    private val userRepo: UserRepo,
    private val securityService: SecurityService,
    private val artistService: ArtistService,
    private val quickEntityCountryRepo: QuickEntityCountryRepo,
    ) : UserService {

    private val userAuthUid = securityService.userAuthUid ?: throw TODO()

    override fun findMyProfile(): UserFullDto? {
        return if (securityService.userAuthUid != null)
            userRepo.findMyProfile(userAuthUid)
        else null
    }

    override fun deleteMyProfile() {
        userRepo.deleteMyProfile(userAuthUid)
    }

    override fun followArtist(id: Long) {
        // todo check if no association already
        userRepo.followArtist(userAuthUid, id)
        artistService.incrementFollowers(id)
    }

    override fun unfollowArtist(id: Long) {
        // todo check if association exists already
        userRepo.unfollowArtist(userAuthUid, id)
        artistService.decrementFollowers(id)
    }

    override fun findMyFollowsArtist(): List<ArtistShortDto> {
        return userRepo.findMyFollowsArtist(userAuthUid)
    }

    override fun findByAuthUidForSecurityService(authUid: String): UserFullDto? {
        return userRepo.findMyProfile(authUid)
    }

    override fun save(dto: UserWriteDto):UserShortDto {
        return userRepo.save(dto, userAuthUid)
    }

   override fun update(dto: UserWriteDto) {
       userRepo.update(dto, userAuthUid)
    }

    override fun findAll(): List<UserShortDto> {
        TODO("Not yet implemented")
    }
}