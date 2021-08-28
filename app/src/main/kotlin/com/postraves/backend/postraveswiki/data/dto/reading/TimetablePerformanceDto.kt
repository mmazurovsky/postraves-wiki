package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
import jooq.tables.records.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class TimetablePerformanceDto(
    val id: Long,
    val artists: List<ArtistShortDto>,
    val typeOfPerformance: String?,
    @Serializable(KOffsetDateTimeSerializer::class)
    val startingDateTime: OffsetDateTime,
    @Serializable(KOffsetDateTimeSerializer::class)
    val endingDateTime: OffsetDateTime?,
) {
    companion object {
        fun createOutOfDbRecords(timetableItemRecord: TimetableItemRecord, artistsWithCountryAndIsFollowed: List<Triple<ArtistRecord, CountryRecord, Boolean>>) : TimetablePerformanceDto {
            return TimetablePerformanceDto(
                id = timetableItemRecord.id ?: throw RecordFieldNullException("Timetable Performance Id"),
                typeOfPerformance = timetableItemRecord.typeOfPerformance,
                startingDateTime = timetableItemRecord.startingDateTime ?: throw RecordFieldNullException("Timetable Performance starting datetime"),
                endingDateTime = timetableItemRecord.endingDateTime,
                artists = artistsWithCountryAndIsFollowed.map { ArtistShortDto.createOutOfDbRecords(it.first, it.second, it.third) }.toList()
            )
        }
    }
}
