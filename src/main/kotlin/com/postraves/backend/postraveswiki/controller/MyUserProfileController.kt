package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.service.followable.MyUserProfileService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class MyUserProfileController(private val myUserProfileService: MyUserProfileService) {

    @GetMapping("/myProfile")
    fun findMyProfile(): UserFullDto? {
        return myUserProfileService.findMyProfile().first
    }

    @PostMapping("/public/myProfile")
    fun save(dto: UserWriteDto):UserShortDto {
        return myUserProfileService.save(dto)
    }

    @PutMapping("/myProfile")
    fun update(dto: UserWriteDto) {
        myUserProfileService.update(dto)
    }

    @DeleteMapping("/myProfile")
    fun deleteMyProfile() {
        myUserProfileService.deleteMyProfile()
    }

    @PostMapping("/myFollows/artist/{id}")
    fun followArtist(@PathVariable id: Long) {
        myUserProfileService.followArtist(id)
    }

    @DeleteMapping("/myFollows/artist/{id}")
    fun unfollowArtist(@PathVariable id: Long)  {
        myUserProfileService.unfollowArtist(id)
    }

    @GetMapping("/myFollows/artist")
    fun findMyFollowsArtist() : List<ArtistShortDto> {
        return myUserProfileService.findMyFollowsArtist()
    }
}