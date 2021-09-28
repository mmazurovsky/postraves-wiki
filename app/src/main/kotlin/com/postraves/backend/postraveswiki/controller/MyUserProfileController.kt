package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.service.followable.MyUserProfileService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class MyUserProfileController(
    private val myUserProfileService: MyUserProfileService
    ) {

    @GetMapping("/public/myProfile")
    @ResponseStatus(HttpStatus.OK)
    fun findMyProfile(): UserFullDto? {
        return myUserProfileService.findMyProfile().first
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

    @PostMapping("/myFollows/artist/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun followArtist(@PathVariable id: Long) {
        myUserProfileService.followArtist(id)
    }

    @DeleteMapping("/myFollows/artist/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun unfollowArtist(@PathVariable id: Long)  {
        myUserProfileService.unfollowArtist(id)
    }

    @GetMapping("/myFollows/artist")
    @ResponseStatus(HttpStatus.OK)
    fun findMyFollowsArtist() : List<ArtistShortDto> {
        return myUserProfileService.findMyFollowsArtist()
    }
}