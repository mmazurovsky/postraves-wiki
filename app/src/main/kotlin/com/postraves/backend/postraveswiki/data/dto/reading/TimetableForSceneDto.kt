package com.postraves.backend.postraveswiki.data.dto.reading

import jooq.tables.records.*
import kotlinx.serialization.Serializable

@Serializable
data class TimetableForSceneDto(
    val scene: SceneDto,
    val performances: List<TimetablePerformanceDto>,
) {
    companion object {
        fun createOutOfDbRecords(sceneRecord: SceneRecord, timetablePerformances: List<Pair<TimetableItemRecord, List<Triple<ArtistRecord, CountryRecord, Boolean>>>>) : TimetableForSceneDto {
            return TimetableForSceneDto(
                scene = SceneDto.createOutOfDbRecords(sceneRecord),
                performances = timetablePerformances.map { TimetablePerformanceDto.createOutOfDbRecords(it.first, it.second) }.toList()
            )
        }
    }
}
