package com.postraves.backend.postraveswiki.data.dto.reading

import com.google.firebase.auth.UserRecord
import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import jooq.tables.records.UserProfileRecord
import kotlinx.serialization.Serializable

@Serializable
data class UserShortDto(
    val name: String,
    val imageLink : String?,
    val overallFollowersCount: Int,
) : BaseShortDto {
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(userRecord: UserProfileRecord) : UserShortDto {
            return UserShortDto(
                name = userRecord.name ?: throw TODO(),
                imageLink = userRecord.imageLink,
                overallFollowersCount = userRecord.overallFollowersCount ?: throw TODO(),
            )
        }
    }
}
