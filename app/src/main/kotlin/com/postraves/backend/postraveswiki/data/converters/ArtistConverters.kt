package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import org.springframework.stereotype.Service

interface ArtistConverters {
    fun createFullDtoFromRecord(
        artistRecord: ArtistRecord,
        countryRecord: CountryRecord,
        isFollowed: Boolean
    ): ArtistFullDto

    fun createShortDtoFromRecord(
        artistRecord: ArtistRecord,
        countryRecord: CountryRecord,
        isFollowed: Boolean
    ): ArtistShortDto

    fun transferDataFromDtoToRecord(dto: ArtistWriteDto, record: ArtistRecord)
    fun createShortDtoFromMap(map: Map<String, String>): ArtistShortDto
}

@Service
class ArtistConvertersImpl(
    private val countryConverters: CountryConverters,
) : ArtistConverters {

    override fun createFullDtoFromRecord(
        artistRecord: ArtistRecord,
        countryRecord: CountryRecord,
        isFollowed: Boolean
    ): ArtistFullDto {
        return ArtistFullDto(
            id = artistRecord.id ?: throw RecordFieldNullException("Artist Id"),
            name = artistRecord.name ?: throw RecordFieldNullException("Artist Name"),
            imageLink = artistRecord.imageLink,
            instagramLink = artistRecord.instagramLink,
            soundcloudLink = artistRecord.soundcloudLink,
            about = artistRecord.about,
            country =
            if (countryRecord.name != null)
                countryConverters.createDtoFromRecord(countryRecord)
            else null,
            isFollowed = isFollowed
        )
    }

    override fun createShortDtoFromRecord(
        artistRecord: ArtistRecord,
        countryRecord: CountryRecord,
        isFollowed: Boolean
    ): ArtistShortDto {
        return ArtistShortDto(
            id = artistRecord.id ?: throw RecordFieldNullException("Artist Id"),
            name = artistRecord.name ?: throw RecordFieldNullException("Artist Name"),
            imageLink = artistRecord.imageLink,
            country =
            if (countryRecord.name != null)
                countryConverters.createDtoFromRecord(countryRecord)
            else null,
            isFollowed = isFollowed
        )

    }

    override fun transferDataFromDtoToRecord(dto: ArtistWriteDto, record: ArtistRecord) {
        record.name = dto.name
        record.imageLink = dto.imageLink
        record.countryName = dto.countryName
        record.soundcloudLink = dto.soundcloudLink
        record.instagramLink = dto.instagramLink
        record.about = dto.about
    }

    @ExperimentalSerializationApi
    override fun createShortDtoFromMap(map: Map<String, String>): ArtistShortDto =
        Properties.decodeFromStringMap(map)
}
