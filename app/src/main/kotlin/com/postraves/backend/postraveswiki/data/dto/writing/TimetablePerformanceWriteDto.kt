package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
import jooq.tables.records.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class TimetablePerformanceWriteDto(
    val id: Long? = null,
    val sceneId: Long? = null,
    val artistIds: Set<Long>,
    val typeOfPerformance: String? = null,
    @Serializable(KOffsetDateTimeSerializer::class)
    val startingDateTime: OffsetDateTime? = null,
    @Serializable(KOffsetDateTimeSerializer::class)
    val endingDateTime: OffsetDateTime? = null,
) : BaseWriteDto
