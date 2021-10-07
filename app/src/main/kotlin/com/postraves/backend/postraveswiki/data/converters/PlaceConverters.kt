package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.CoordinateDto
import com.postraves.backend.postraveswiki.data.dto.reading.PlaceFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.PlaceShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.PlaceWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import jooq.tables.records.PlaceRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import org.springframework.stereotype.Service

interface PlaceConverters {
    fun createFullDtoFromRecord(placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : PlaceFullDto
    fun createShortDtoFromRecord(placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : PlaceShortDto
    fun transferDataFromDtoToRecord(dto: PlaceWriteDto, record: PlaceRecord)
    fun createShortDtoFromMap(map: Map<String, String>): PlaceShortDto
}

@Service
class PlaceConvertersImpl(
    private val cityConverters: CityConverters,
) : PlaceConverters {
    override fun createFullDtoFromRecord(
        placeRecord: PlaceRecord,
        cityRecord: CityRecord,
        countryRecord: CountryRecord,
        isFollowed: Boolean
    ): PlaceFullDto {
        return PlaceFullDto(
            id = placeRecord.placeId ?: throw RecordFieldNullException("Place Id"),
            name = placeRecord.placeName ?: throw RecordFieldNullException("Place Name"),
            imageLink = placeRecord.placeImageLink,
            streetAddress = placeRecord.placeStreetAddress ?: throw RecordFieldNullException("Place Street Address"),
            coordinate = CoordinateDto(
                latitude = placeRecord.placeLatitude ?: throw RecordFieldNullException("Place Latitude"),
                longitude = placeRecord.placeLongitude ?: throw RecordFieldNullException("Place Longitude")
            ),
            city = cityConverters.createDtoFromRecord(cityRecord, countryRecord),
            soundcloudLink = placeRecord.placeSoundcloudUsername,
            instagramLink = placeRecord.placeInstagramUsername,
            about = placeRecord.placeAbout,
            isFollowed = isFollowed,
        )
    }

    override fun createShortDtoFromRecord(
        placeRecord: PlaceRecord,
        cityRecord: CityRecord,
        countryRecord: CountryRecord,
        isFollowed: Boolean
    ): PlaceShortDto {
        return PlaceShortDto(
            id = placeRecord.placeId ?: throw RecordFieldNullException("Place Id"),
            name = placeRecord.placeName ?: throw RecordFieldNullException("Place Name"),
            imageLink = placeRecord.placeImageLink,
            streetAddress = placeRecord.placeStreetAddress ?: throw RecordFieldNullException("Place Street Address"),
            coordinate = CoordinateDto(
                latitude = placeRecord.placeLatitude ?: throw RecordFieldNullException("Place Latitude"),
                longitude = placeRecord.placeLongitude ?: throw RecordFieldNullException("Place Longitude")
            ),
            city = cityConverters.createDtoFromRecord(cityRecord, countryRecord),
            isFollowed = isFollowed
        )
    }

    override fun transferDataFromDtoToRecord(dto: PlaceWriteDto, record: PlaceRecord) {
        record.placeName = dto.name
        record.placeImageLink = dto.imageLink
        record.placeCityName = dto.cityName
        record.placeSoundcloudUsername = dto.soundcloudUsername
        record.placeInstagramUsername = dto.instagramUsername
        record.placeAbout = dto.about
        record.placeLatitude = dto.coordinate.latitude
        record.placeLongitude = dto.coordinate.longitude
        record.placeStreetAddress = dto.streetAddress
    }

    @ExperimentalSerializationApi
    override fun createShortDtoFromMap(map: Map<String, String>): PlaceShortDto {
        return Properties.decodeFromStringMap(map)
    }
}
