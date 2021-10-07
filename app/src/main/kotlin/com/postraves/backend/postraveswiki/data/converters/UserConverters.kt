package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import jooq.tables.records.UserProfileRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import org.springframework.stereotype.Service

interface UserConverters {
    fun createFullDtoFromRecord(
        userRecord: UserProfileRecord,
        cityRecord: CityRecord,
        countryRecord: CountryRecord
    ): UserFullDto

    fun createShortDtoFromRecord(userRecord: UserProfileRecord): UserShortDto
    fun transferDataFromDtoToRecord(dto: UserWriteDto, record: UserProfileRecord)
    fun createShortDtoFromMap(map: Map<String, String>): UserShortDto
}

@Service
class UserConvertersImpl(
    private val cityConverters: CityConverters,
) : UserConverters {

    override fun createFullDtoFromRecord(
        userRecord: UserProfileRecord,
        cityRecord: CityRecord,
        countryRecord: CountryRecord
    ): UserFullDto {
        return UserFullDto(
            id = userRecord.userProfileId ?: throw RecordFieldNullException("User Id"),
            name = userRecord.userProfileName ?: throw RecordFieldNullException("User Name"),
            imageLink = userRecord.userProfileImageLink,
            currentCity = cityConverters.createDtoFromRecord(cityRecord, countryRecord),
            telegramUsername = userRecord.userProfileTelegramUsername,
            instagramUsername = userRecord.userProfileInstagramUsername,
            about = userRecord.userProfileAbout,
        )
    }

    override fun createShortDtoFromRecord(userRecord: UserProfileRecord): UserShortDto {
        return UserShortDto(
            id = userRecord.userProfileId ?: throw RecordFieldNullException("User Id"),
            name = userRecord.userProfileName ?: throw RecordFieldNullException("User Name"),
            imageLink = userRecord.userProfileImageLink,
        )
    }


    override fun transferDataFromDtoToRecord(dto: UserWriteDto, record: UserProfileRecord) {
        record.userProfileName = dto.name
        record.userProfileImageLink = dto.imageLink
        record.userProfileCityName = dto.currentCity
        record.userProfileTelegramUsername = dto.telegramUsername
        record.userProfileInstagramUsername = dto.instagramUsername
        record.userProfileAbout = dto.about
    }

    @ExperimentalSerializationApi
    override fun createShortDtoFromMap(map: Map<String, String>): UserShortDto =
        Properties.decodeFromStringMap(map)
}
