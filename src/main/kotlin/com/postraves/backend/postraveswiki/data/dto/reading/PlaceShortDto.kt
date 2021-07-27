package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.*
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import jooq.tables.records.PlaceRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import kotlinx.serialization.properties.encodeToStringMap

@Serializable
data class PlaceShortDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    val city: CityDto,
    val streetAddress: String,
    val coordinate: CoordinateDto,
    val isFollowed: Boolean = false,
    override val overallFollowers: Int = 0,
    override val weeklyFollowers: Int = 0,
    ) : BaseShortDtoWithIdAndRating<PlaceShortDto> {
    companion object {
        fun createOutOfDbRecords(placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : PlaceShortDto {
            return PlaceShortDto(
                id = placeRecord.id ?: throw RecordFieldNullException("Place Id"),
                name = placeRecord.name ?: throw RecordFieldNullException("Place Name"),
                imageLink = placeRecord.imageLink,
                streetAddress = placeRecord.streetAddress ?: throw RecordFieldNullException("Place Street Address"),
                coordinate = CoordinateDto(
                    latitude = placeRecord.latitude ?: throw RecordFieldNullException("Place Latitude"),
                    longitude = placeRecord.longitude ?: throw RecordFieldNullException("Place Longitude")
                ),
                city = CityDto.createOutOfDbRecords(cityRecord, countryRecord),
                isFollowed = isFollowed
            )
        }

        @ExperimentalSerializationApi
        fun fromMap(map: Map<String, String>): PlaceShortDto =
            Properties.decodeFromStringMap(map)
    }

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> = Properties.encodeToStringMap(value = this)

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): PlaceShortDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}