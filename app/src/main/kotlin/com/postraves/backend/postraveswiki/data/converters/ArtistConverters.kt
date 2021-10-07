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
            id = artistRecord.artistId ?: throw RecordFieldNullException("Artist Id"),
            name = artistRecord.artistName ?: throw RecordFieldNullException("Artist Name"),
            imageLink = artistRecord.artistImageLink,
            instagramLink = artistRecord.artistInstagramUsername,
            soundcloudLink = artistRecord.artistSoundcloudUsername,
            about = artistRecord.artistAbout,
            country =
            if (countryRecord.countryName != null)
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
            id = artistRecord.artistId ?: throw RecordFieldNullException("Artist Id"),
            name = artistRecord.artistName ?: throw RecordFieldNullException("Artist Name"),
            imageLink = artistRecord.artistImageLink,
            country =
            if (countryRecord.countryName != null)
                countryConverters.createDtoFromRecord(countryRecord)
            else null,
            isFollowed = isFollowed
        )

    }

    override fun transferDataFromDtoToRecord(dto: ArtistWriteDto, record: ArtistRecord) {
        record.artistName = dto.name
        record.artistImageLink = dto.imageLink
        record.artistCountryName = dto.countryName
        record.artistSoundcloudUsername = dto.soundcloudUsername
        record.artistInstagramUsername = dto.instagramUsername
        record.artistAbout = dto.about
    }

    @ExperimentalSerializationApi
    override fun createShortDtoFromMap(map: Map<String, String>): ArtistShortDto =
        Properties.decodeFromStringMap(map)
}
