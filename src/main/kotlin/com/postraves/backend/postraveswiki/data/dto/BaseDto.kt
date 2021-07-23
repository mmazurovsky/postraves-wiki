package com.postraves.backend.postraveswiki.data.dto

interface BaseDto

interface BaseShortDto : BaseDto

interface BaseFullDto : BaseDto

interface BaseWriteDto : BaseDto

interface BaseIdDto : BaseDto {
    val id: Long
}

interface BaseRatingDtoWithId <T> : BaseIdDto {
    val overallFollowers: Int
    val weeklyFollowers: Int
    fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): T
}