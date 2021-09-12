package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.reading.UnityFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UnityWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CountryRecord
import jooq.tables.records.UnityRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import org.springframework.stereotype.Service

interface UnityConverters {
    fun createFullDtoFromRecord(unityRecord: UnityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : UnityFullDto
    fun createShortDtoFromRecord(unityRecord: UnityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : UnityShortDto
    fun transferDataFromDtoToRecord(dto: UnityWriteDto, record: UnityRecord)
    fun createShortDtoFromMap(map: Map<String, String>): UnityShortDto
}

@Service
class UnityConvertersImpl(
    private val countryConverters: CountryConverters,
) : UnityConverters {

    override fun createFullDtoFromRecord(unityRecord: UnityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : UnityFullDto {
        return UnityFullDto(
            id = unityRecord.id ?: throw RecordFieldNullException("Unity Id"),
            name = unityRecord.name ?: throw RecordFieldNullException("Unity Name"),
            imageLink = unityRecord.imageLink,
            instagramLink = unityRecord.instagramLink,
            soundcloudLink = unityRecord.soundcloudLink,
            bandcampLink = unityRecord.bandcampLink,
            about = unityRecord.about,
            country =
            if (countryRecord.name != null)
                countryConverters.createDtoFromRecord(countryRecord)
            else null,
            isFollowed = isFollowed
        )
    }

    override fun createShortDtoFromRecord(unityRecord: UnityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : UnityShortDto {
        return UnityShortDto(
            id = unityRecord.id ?: throw RecordFieldNullException("Unity Id"),
            name = unityRecord.name ?: throw RecordFieldNullException("Unity Name"),
            imageLink = unityRecord.imageLink,
            country =
            if (countryRecord.name != null)
                countryConverters.createDtoFromRecord(countryRecord)
            else null,
            isFollowed = isFollowed
        )

    }

    override fun transferDataFromDtoToRecord(dto: UnityWriteDto, record: UnityRecord) {
        record.name = dto.name
        record.imageLink = dto.imageLink
        record.countryName = dto.countryName
        record.soundcloudLink = dto.soundcloudLink
        record.instagramLink = dto.instagramLink
        record.bandcampLink = dto.bandcampLink
        record.about = dto.about
    }

    @ExperimentalSerializationApi
    override fun createShortDtoFromMap(map: Map<String, String>): UnityShortDto =
        Properties.decodeFromStringMap(map)
}
