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
    private val countryConverters: CountryConverters,
    private val cityConverters: CityConverters,
) : UserConverters {

    override fun createFullDtoFromRecord(
        userRecord: UserProfileRecord,
        cityRecord: CityRecord,
        countryRecord: CountryRecord
    ): UserFullDto {
        return UserFullDto(
            name = userRecord.name ?: throw RecordFieldNullException("User Name"),
            imageLink = userRecord.imageLink,
            currentCity = cityConverters.createDtoFromRecord(cityRecord, countryRecord),
            telegramLink = userRecord.telegramLink,
            instagramLink = userRecord.instagramLink,
            about = userRecord.about,
        )
    }

    override fun createShortDtoFromRecord(userRecord: UserProfileRecord): UserShortDto {
        return UserShortDto(
            name = userRecord.name ?: throw RecordFieldNullException("User Name"),
            imageLink = userRecord.imageLink,
        )
    }


    override fun transferDataFromDtoToRecord(dto: UserWriteDto, record: UserProfileRecord) {
        record.name = dto.name
        record.imageLink = dto.imageLink
        record.cityName = dto.currentCity
        record.telegramLink = dto.telegramLink
        record.instagramLink = dto.instagramLink
        record.about = dto.about
    }

    @ExperimentalSerializationApi
    override fun createShortDtoFromMap(map: Map<String, String>): UserShortDto =
        Properties.decodeFromStringMap(map)
}
