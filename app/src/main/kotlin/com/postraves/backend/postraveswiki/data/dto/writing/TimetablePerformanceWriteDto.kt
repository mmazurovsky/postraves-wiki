package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
import jooq.tables.records.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class TimetablePerformanceWriteDto(
    val id: Long?,
    val sceneId: Long?,
    val artistIds: Set<Long>,
    val typeOfPerformance: String?,
    @Serializable(KOffsetDateTimeSerializer::class)
    val startingDateTime: OffsetDateTime?,
    @Serializable(KOffsetDateTimeSerializer::class)
    val endingDateTime: OffsetDateTime?,
) {
    fun transferDataToDbRecord(record: TimetableItemRecord) {
        record.typeOfPerformance = typeOfPerformance
        record.startingDateTime = startingDateTime
        record.endingDateTime = endingDateTime
        record.sceneId = sceneId
    }

    companion object {
        fun createOutOfDbRecords(timetableItemRecord: TimetableItemRecord, artistIds: Set<Long>) : TimetablePerformanceWriteDto {
            return TimetablePerformanceWriteDto(
                id = timetableItemRecord.id ?: throw RecordFieldNullException("Timetable Performance Id"),
                typeOfPerformance = timetableItemRecord.typeOfPerformance,
                startingDateTime = timetableItemRecord.startingDateTime,
                endingDateTime = timetableItemRecord.endingDateTime,
                sceneId = timetableItemRecord.sceneId,
                artistIds = artistIds
            )
        }
    }
}
