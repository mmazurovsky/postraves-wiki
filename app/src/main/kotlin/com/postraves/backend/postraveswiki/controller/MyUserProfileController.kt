package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.security.SecurityService
import com.postraves.backend.postraveswiki.service.followable.MyUserProfileService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class MyUserProfileController(
    private val myUserProfileService: MyUserProfileService,
    private val securityService: SecurityService,
    ) {

    @GetMapping("/public/myProfile")
    @ResponseStatus(HttpStatus.OK)
    fun findMyProfile(): UserFullDto? {
        return securityService.user
    }

    @PostMapping("/public/myProfile")
    @ResponseStatus(HttpStatus.CREATED)
    fun save(@RequestBody dto: UserWriteDto): UserShortDto {
        return myUserProfileService.save(dto)
    }

    @PutMapping("/myProfile")
    @ResponseStatus(HttpStatus.OK)
    fun update(@RequestBody dto: UserWriteDto) {
        myUserProfileService.update(dto)
    }

    @DeleteMapping("/myProfile")
    @ResponseStatus(HttpStatus.OK)
    fun deleteMyProfile() {
        myUserProfileService.deleteMyProfile()
    }

    @GetMapping("/public/nicknameCheck/{nickname}")
    @ResponseStatus(HttpStatus.OK)
    fun checkNickname(@PathVariable nickname: String): Boolean {
        return myUserProfileService.checkNicknameIsFree(nickname)
    }

    @PostMapping("/myFollowing/artist/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun followArtist(@PathVariable id: Long) {
        myUserProfileService.followArtist(id)
    }

    @PostMapping("/myFollowing/event/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun followEvent(@PathVariable id: Long) {
        myUserProfileService.followEvent(id)
    }

    @PostMapping("/myFollowing/place/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun followPlace(@PathVariable id: Long) {
        myUserProfileService.followPlace(id)
    }

    @PostMapping("/myFollowing/unity/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun followUnity(@PathVariable id: Long) {
        myUserProfileService.followUnity(id)
    }

    @DeleteMapping("/myFollowing/artist/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun unfollowArtist(@PathVariable id: Long)  {
        myUserProfileService.unfollowArtist(id)
    }

    @DeleteMapping("/myFollowing/event/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun unfollowEvent(@PathVariable id: Long)  {
        myUserProfileService.unfollowEvent(id)
    }

    @DeleteMapping("/myFollowing/place/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun unfollowPlace(@PathVariable id: Long)  {
        myUserProfileService.unfollowPlace(id)
    }

    @DeleteMapping("/myFollowing/unity/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun unfollowUnity(@PathVariable id: Long)  {
        myUserProfileService.unfollowUnity(id)
    }

    @GetMapping("/myFollowing/artist")
    @ResponseStatus(HttpStatus.OK)
    fun findMyFollowingArtist() : List<ArtistShortDto> {
        return myUserProfileService.findMyFollowingArtists()
    }

    @GetMapping("/myFollowing/event")
    @ResponseStatus(HttpStatus.OK)
    fun findMyFollowingEvent() : List<EventShortDto> {
        return myUserProfileService.findMyFollowingEvents()
    }

    @GetMapping("/myFollowing/place")
    @ResponseStatus(HttpStatus.OK)
    fun findMyFollowingPlace() : List<PlaceShortDto> {
        return myUserProfileService.findMyFollowingPlaces()
    }

    @GetMapping("/myFollowing/unity")
    @ResponseStatus(HttpStatus.OK)
    fun findMyFollowingUnity() : List<UnityShortDto> {
        return myUserProfileService.findMyFollowingUnities()
    }
}