package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.util.KOffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class TimetablePerformanceDto(
    val id: Long,
    val artists: List<ArtistShortDto>,
    val typeOfPerformance: String?,
    @Serializable(KOffsetDateTimeSerializer::class)
    val startingDateTime: OffsetDateTime?,
    @Serializable(KOffsetDateTimeSerializer::class)
    val endingDateTime: OffsetDateTime?,
)
