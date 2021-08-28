package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.util.KLocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.*

@Serializable
data class EventsByDateDto(
    @Serializable(KLocalDateSerializer::class)
    val date: LocalDate,
    val events: MutableList<EventShortDto>,
)
