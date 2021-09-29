package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import jooq.tables.records.UserProfileRecord
import kotlinx.serialization.Serializable

@Serializable
data class UserWriteDto(
    val name: String,
    val currentCity: String,
    val imageLink : String? = null,
    val telegramLink: String? = null,
    val instagramLink: String? = null,
    val about: String? = null,
) : BaseWriteDto
