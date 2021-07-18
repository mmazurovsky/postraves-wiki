package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) : BaseRequests<UserWriteDto, UserShortDto> {

    @GetMapping("/myProfile")
    fun findMyProfile(): UserFullDto {
        return userService.findMyProfile()
    }

    @PostMapping("/public/myProfile")
    override fun save(dto: UserWriteDto):UserShortDto {
        return userService.save(dto)
    }

    @PutMapping("/myProfile")
    override fun update(dto: UserWriteDto) {
        userService.update(dto)
    }

    @DeleteMapping("/myProfile")
    fun deleteMyProfile() {
        userService.deleteMyProfile()
    }

    @PostMapping("/myFollows/artist/{id}")
    fun followArtist(@PathVariable id: Long) {
        userService.followArtist(id)
    }

    @DeleteMapping("/myFollows/artist/{id}")
    fun unfollowArtist(@PathVariable id: Long)  {
        userService.unfollowArtist(id)
    }

    @GetMapping("/myFollows/artist")
    fun findMyFollowsArtist() : List<ArtistShortDto> {
        return userService.findMyFollowsArtist()
    }

    // todo certain roles access
    override fun findAll(): List<UserShortDto> {
        TODO("Not yet implemented")
    }

}