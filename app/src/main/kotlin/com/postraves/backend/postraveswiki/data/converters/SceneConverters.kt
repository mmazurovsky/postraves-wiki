package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.SceneRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import org.springframework.stereotype.Service

interface SceneConverters {
    fun createDtoFromRecord(sceneRecord: SceneRecord): SceneDto
    fun transferDataFromDtoToRecord(dto: SceneDto, record: SceneRecord)
}

@Service
class SceneConvertersImpl : SceneConverters {

    override fun createDtoFromRecord(
        sceneRecord: SceneRecord,
    ): SceneDto {
        return SceneDto(
            id = sceneRecord.sceneId ?: throw RecordFieldNullException("Scene Id"),
            name = sceneRecord.sceneName ?: throw RecordFieldNullException("Scene Name"),
            imageLink = sceneRecord.sceneImageLink,
            priority = sceneRecord.scenePriority ?: throw RecordFieldNullException("Scene Priority"),
        )
    }

    override fun transferDataFromDtoToRecord(dto: SceneDto, record: SceneRecord) {
        record.sceneName = dto.name
        record.sceneImageLink = dto.imageLink
        record.scenePriority = dto.priority
    }
}
