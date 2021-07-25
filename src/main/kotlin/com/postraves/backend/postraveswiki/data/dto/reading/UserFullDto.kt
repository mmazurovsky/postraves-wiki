package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import jooq.tables.records.UserProfileRecord
import kotlinx.serialization.Serializable

@Serializable
data class UserFullDto(
    val name: String,
    val imageLink : String?,
    val currentCity: CityDto,
    val telegramLink: String?,
    val instagramLink: String?,
    val about: String?,
) : BaseFullDto {
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(userRecord: UserProfileRecord, cityRecord: CityRecord, countryRecord: CountryRecord) : UserFullDto {
            return UserFullDto(
                name = userRecord.name ?: throw RecordFieldNullException("User Name"),
                imageLink = userRecord.imageLink,
                currentCity = CityDto.createOutOfDbRecords(cityRecord, countryRecord),
                telegramLink = userRecord.telegramLink,
                instagramLink = userRecord.instagramLink,
                about = userRecord.about,
            )
        }
    }
}
