package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.CoordinateDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
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
            id = placeRecord.id ?: throw RecordFieldNullException("Place Id"),
            name = placeRecord.name ?: throw RecordFieldNullException("Place Name"),
            imageLink = placeRecord.imageLink,
            streetAddress = placeRecord.streetAddress ?: throw RecordFieldNullException("Place Street Address"),
            coordinate = CoordinateDto(
                latitude = placeRecord.latitude ?: throw RecordFieldNullException("Place Latitude"),
                longitude = placeRecord.longitude ?: throw RecordFieldNullException("Place Longitude")
            ),
            city = cityConverters.createDtoFromRecord(cityRecord, countryRecord),
            soundcloudLink = placeRecord.soundcloudLink,
            instagramLink = placeRecord.instagramLink,
            about = placeRecord.about,
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
            id = placeRecord.id ?: throw RecordFieldNullException("Place Id"),
            name = placeRecord.name ?: throw RecordFieldNullException("Place Name"),
            imageLink = placeRecord.imageLink,
            streetAddress = placeRecord.streetAddress ?: throw RecordFieldNullException("Place Street Address"),
            coordinate = CoordinateDto(
                latitude = placeRecord.latitude ?: throw RecordFieldNullException("Place Latitude"),
                longitude = placeRecord.longitude ?: throw RecordFieldNullException("Place Longitude")
            ),
            city = cityConverters.createDtoFromRecord(cityRecord, countryRecord),
            isFollowed = isFollowed
        )
    }

    override fun transferDataFromDtoToRecord(dto: PlaceWriteDto, record: PlaceRecord) {
        record.name = dto.name
        record.imageLink = dto.imageLink
        record.cityName = dto.cityName
        record.soundcloudLink = dto.soundcloudLink
        record.instagramLink = dto.instagramLink
        record.about = dto.about
        record.latitude = dto.coordinate.latitude
        record.longitude = dto.coordinate.longitude
        record.streetAddress = dto.streetAddress
    }

    @ExperimentalSerializationApi
    override fun createShortDtoFromMap(map: Map<String, String>): PlaceShortDto {
        return Properties.decodeFromStringMap(map)
    }
}
