package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.ArtistRecord
import jooq.tables.records.SceneRecord
import kotlinx.serialization.Serializable

@Serializable
data class SceneDto(
    val id: Long?,
    val name: String,
    val imageLink: String,
    val priority: Int,
) : BaseWriteDto, BaseShortDto, BaseFullDto {

    companion object FactoryDbRecord {
        fun createOutOfDbRecords(sceneRecord: SceneRecord): SceneDto {
            return SceneDto(
                id = sceneRecord.id ?: throw RecordFieldNullException("Scene Id"),
                name = sceneRecord.name ?: throw RecordFieldNullException("Scene Name"),
                imageLink = sceneRecord.imageLink ?: throw RecordFieldNullException("Scene Name"),
                priority = sceneRecord.priority ?: throw RecordFieldNullException("Scene Priority"),
            )
        }
    }

    fun transferDataToDbRecord(record: SceneRecord) {
        record.name = name
        record.imageLink = imageLink
        record.priority = priority
    }
}

