package com.postraves.backend.postraveswiki.data.dto

interface BaseDto

interface BaseIdDto : BaseDto {
    val id: Long
}

interface FollowableDto<T> : BaseIdDto {
    val overallFollowers: Int
    val weeklyFollowers: Int
    fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): T
}

interface ConvertableToMap {
    fun toMap(): Map<String, String>
}

interface BaseShortDto : BaseDto
interface BaseShortDtoWithId : BaseShortDto, BaseIdDto
interface FollowableShortDto<T> : BaseShortDtoWithId, FollowableDto<T>

interface BaseFullDto : BaseDto
interface BaseFullDtoWithId : BaseFullDto, BaseIdDto
interface FollowableFullDto<T> : BaseFullDtoWithId, FollowableDto<T>

interface BaseWriteDto : BaseDto
