package com.postraves.backend.postraveswiki.data.dto

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto

interface BaseDto

interface BaseIdDto : BaseDto {
    val id: Long
}

interface BaseRatingDtoWithId<T> : BaseIdDto {
    val overallFollowers: Int
    val weeklyFollowers: Int
    fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): T
    fun asMap(): Map<String, String>
}

interface BaseShortDto : BaseDto
interface BaseShortDtoWithId : BaseShortDto, BaseIdDto
interface BaseShortDtoWithIdAndRating<T> : BaseShortDtoWithId, BaseRatingDtoWithId<T>

interface BaseFullDto : BaseDto
interface BaseFullDtoWithId : BaseFullDto, BaseIdDto
interface BaseFullDtoWithIdAndRating<T> : BaseFullDtoWithId, BaseRatingDtoWithId<T>

interface BaseWriteDto : BaseDto
