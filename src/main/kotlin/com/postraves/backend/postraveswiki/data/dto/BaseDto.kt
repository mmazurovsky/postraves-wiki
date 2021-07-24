package com.postraves.backend.postraveswiki.data.dto

interface BaseDto

interface BaseIdDto : BaseDto {
    val id: Long
}

//interface BaseRatingDtoWithId : BaseIdDto {
//    val overallFollowers: Int
//    val weeklyFollowers: Int
////    fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): T
//}

interface BaseShortDto : BaseDto
interface BaseShortDtoWithId : BaseShortDto, BaseIdDto

interface BaseFullDto : BaseDto
interface BaseFullDtoWithId : BaseFullDto, BaseIdDto

interface BaseWriteDto : BaseDto