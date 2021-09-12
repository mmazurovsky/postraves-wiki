package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.UserProfileRecord
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class UserShortDto(
    val name: String,
    val imageLink : String?,
    @Required
    val overallFollowers: Int = 0,
    @Required
    val weeklyFollowers: Int = 0,
) : BaseShortDto
