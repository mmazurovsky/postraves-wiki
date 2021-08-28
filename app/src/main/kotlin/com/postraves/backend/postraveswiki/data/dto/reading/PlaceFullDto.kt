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
data class PlaceFullDto(
    override val id: Long,
    val name: String,
    val imageLink : String?,
    val city: CityDto,
    val streetAddress: String,
    val coordinate: CoordinateDto,
    val soundcloudLink: String?,
    val instagramLink: String?,
    val about: String?,
    val isFollowed: Boolean = false,
    override val overallFollowers: Int = 0,
    override val weeklyFollowers: Int = 0,
    ) : FollowableFullDto<PlaceFullDto>, ConvertableToMap {
    companion object {
        fun createOutOfDbRecords(placeRecord: PlaceRecord, cityRecord: CityRecord, countryRecord: CountryRecord, isFollowed: Boolean) : PlaceFullDto {
            return PlaceFullDto(
                id = placeRecord.id ?: throw RecordFieldNullException("Place Id"),
                name = placeRecord.name ?: throw RecordFieldNullException("Place Name"),
                imageLink = placeRecord.imageLink,
                streetAddress = placeRecord.streetAddress ?: throw RecordFieldNullException("Place Street Address"),
                coordinate = CoordinateDto(
                    latitude = placeRecord.latitude ?: throw RecordFieldNullException("Place Latitude"),
                    longitude = placeRecord.longitude ?: throw RecordFieldNullException("Place Longitude")
                ),
                city = CityDto.createOutOfDbRecords(cityRecord, countryRecord),
                soundcloudLink = placeRecord.soundcloudLink,
                instagramLink = placeRecord.instagramLink,
                about = placeRecord.about,
                isFollowed = isFollowed,
            )
        }

        @ExperimentalSerializationApi
        fun fromMap(map: Map<String, String>): PlaceFullDto =
            Properties.decodeFromStringMap(map)
    }

    @ExperimentalSerializationApi
    override fun toMap(): Map<String, String> = Properties.encodeToStringMap(value = this)

    override fun copyWithFollowersEnriched(overallFollowers: Int, weeklyFollowers: Int): PlaceFullDto {
        return this.copy(overallFollowers = overallFollowers, weeklyFollowers = weeklyFollowers)
    }
}