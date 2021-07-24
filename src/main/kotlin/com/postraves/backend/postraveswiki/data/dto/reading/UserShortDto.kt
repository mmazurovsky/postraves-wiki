package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import jooq.tables.records.UserProfileRecord
import kotlinx.serialization.Serializable

@Serializable
data class UserShortDto(
    val name: String,
    val imageLink : String?,
    val overallFollowers: Int = 0,
    val weeklyFollowers: Int = 0,
) : BaseShortDto {
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(userRecord: UserProfileRecord) : UserShortDto {
            return UserShortDto(
                name = userRecord.name ?: throw TODO(),
                imageLink = userRecord.imageLink,
            )
        }
    }
}
