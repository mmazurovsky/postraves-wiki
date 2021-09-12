package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.reading.TimetableForSceneDto
import com.postraves.backend.postraveswiki.data.dto.reading.TimetablePerformanceDto
import com.postraves.backend.postraveswiki.data.dto.writing.TimetablePerformanceWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import jooq.tables.records.SceneRecord
import jooq.tables.records.TimetableItemRecord
import org.springframework.stereotype.Service
import java.time.ZoneOffset

interface TimetableConverters {
    fun createTimetableForSceneDto(
        sceneRecord: SceneRecord,
        timetablePerformances: List<Pair<TimetableItemRecord, List<Triple<ArtistRecord, CountryRecord, Boolean>>>>,
        offsetFromUtcForThePlace: Int,
    ): TimetableForSceneDto

    fun createTimetablePerformanceDto(
        timetableItemRecord: TimetableItemRecord,
        artistsWithCountryAndIsFollowed: List<Triple<ArtistRecord, CountryRecord, Boolean>>,
        offsetFromUtcForThePlace: Int,
    ): TimetablePerformanceDto

    fun createTimetablePerformanceWriteDto(
        timetableItemRecord: TimetableItemRecord,
        artistIds: Set<Long>
    ): TimetablePerformanceWriteDto

    fun transferDataFromDtoToRecord(dto: TimetablePerformanceWriteDto, record: TimetableItemRecord)
}

@Service
class TimetableConvertersImpl(
    private val artistConverters: ArtistConverters,
    private val sceneConverters: SceneConverters,
) : TimetableConverters {
    override fun createTimetableForSceneDto(
        sceneRecord: SceneRecord,
        timetablePerformances: List<Pair<TimetableItemRecord, List<Triple<ArtistRecord, CountryRecord, Boolean>>>>,
        offsetFromUtcForThePlace: Int,
    ): TimetableForSceneDto {
        return TimetableForSceneDto(
            scene = sceneConverters.createDtoFromRecord(sceneRecord),
            performances = timetablePerformances
                .map {
                    createTimetablePerformanceDto(it.first, it.second, offsetFromUtcForThePlace)
                }.toList()
        )
    }

    override fun createTimetablePerformanceDto(
        timetableItemRecord: TimetableItemRecord,
        artistsWithCountryAndIsFollowed: List<Triple<ArtistRecord, CountryRecord, Boolean>>,
        offsetFromUtcForThePlace: Int,
    ): TimetablePerformanceDto {

        val startDateTimeWithTimeZone = timetableItemRecord.startingDateTime?.toInstant()?.atOffset(ZoneOffset.ofHours(offsetFromUtcForThePlace)) ?: throw RecordFieldNullException("Timetable Performance starting datetime")
        val endDateTimeWithTimeZone = timetableItemRecord.endingDateTime?.toInstant()?.atOffset(ZoneOffset.ofHours(offsetFromUtcForThePlace)) ?: throw RecordFieldNullException("Timetable Performance ending datetime")

        return TimetablePerformanceDto(
            id = timetableItemRecord.id ?: throw RecordFieldNullException("Timetable Performance Id"),
            typeOfPerformance = timetableItemRecord.typeOfPerformance,
            startingDateTime = startDateTimeWithTimeZone,
            endingDateTime = endDateTimeWithTimeZone,
            artists = artistsWithCountryAndIsFollowed
                .map {
                    artistConverters.createShortDtoFromRecord(it.first, it.second, it.third)
                }.toList()
        )
    }

    override fun createTimetablePerformanceWriteDto(
        timetableItemRecord: TimetableItemRecord,
        artistIds: Set<Long>
    ): TimetablePerformanceWriteDto {
        return TimetablePerformanceWriteDto(
            id = timetableItemRecord.id ?: throw RecordFieldNullException("Timetable Performance Id"),
            typeOfPerformance = timetableItemRecord.typeOfPerformance,
            startingDateTime = timetableItemRecord.startingDateTime,
            endingDateTime = timetableItemRecord.endingDateTime,
            sceneId = timetableItemRecord.sceneId,
            artistIds = artistIds
        )
    }

    override fun transferDataFromDtoToRecord(dto: TimetablePerformanceWriteDto, record: TimetableItemRecord) {
        record.typeOfPerformance = dto.typeOfPerformance
        record.startingDateTime = dto.startingDateTime
        record.endingDateTime = dto.endingDateTime
        record.sceneId = dto.sceneId
    }
}
