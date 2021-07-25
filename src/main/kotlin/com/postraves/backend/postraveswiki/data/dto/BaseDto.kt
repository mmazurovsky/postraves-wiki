package com.postraves.backend.postraveswiki.data.dto

interface BaseDto

interface BaseIdDto : BaseDto {
    val id: Long
}

interface BaseRatingDtoWithId<T> : BaseIdDto {
    val overallFollowers: Int
    val weeklyFollowers: Int
    fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): T
}

interface BaseShortDto : BaseDto
interface BaseShortDtoWithId : BaseShortDto, BaseIdDto
interface BaseShortDtoWithIdAndRating<T> : BaseShortDtoWithId, BaseRatingDtoWithId<T>

interface BaseFullDto : BaseDto
interface BaseFullDtoWithId : BaseFullDto, BaseIdDto
interface BaseFullDtoWithIdAndRating<T> : BaseFullDtoWithId, BaseRatingDtoWithId<T>

interface BaseWriteDto : BaseDto
