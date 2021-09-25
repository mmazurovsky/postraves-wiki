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
            id = unityRecord.unityId ?: throw RecordFieldNullException("Unity Id"),
            name = unityRecord.unityName ?: throw RecordFieldNullException("Unity Name"),
            imageLink = unityRecord.unityImageLink,
            instagramLink = unityRecord.unityInstagramLink,
            soundcloudLink = unityRecord.unitySoundcloudLink,
            bandcampLink = unityRecord.unityBandcampLink,
            about = unityRecord.unityAbout,
            country =
            if (countryRecord.countryName != null)
                countryConverters.createDtoFromRecord(countryRecord)
            else null,
            isFollowed = isFollowed
        )
    }

    override fun createShortDtoFromRecord(unityRecord: UnityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : UnityShortDto {
        return UnityShortDto(
            id = unityRecord.unityId ?: throw RecordFieldNullException("Unity Id"),
            name = unityRecord.unityName ?: throw RecordFieldNullException("Unity Name"),
            imageLink = unityRecord.unityImageLink,
            country =
            if (countryRecord.countryName != null)
                countryConverters.createDtoFromRecord(countryRecord)
            else null,
            isFollowed = isFollowed
        )

    }

    override fun transferDataFromDtoToRecord(dto: UnityWriteDto, record: UnityRecord) {
        record.unityName = dto.name
        record.unityImageLink = dto.imageLink
        record.unityCountryName = dto.countryName
        record.unitySoundcloudLink = dto.soundcloudLink
        record.unityInstagramLink = dto.instagramLink
        record.unityBandcampLink = dto.bandcampLink
        record.unityAbout = dto.about
    }

    @ExperimentalSerializationApi
    override fun createShortDtoFromMap(map: Map<String, String>): UnityShortDto =
        Properties.decodeFromStringMap(map)
}
