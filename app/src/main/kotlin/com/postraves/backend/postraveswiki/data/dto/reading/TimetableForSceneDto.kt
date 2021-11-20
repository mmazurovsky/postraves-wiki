package com.postraves.backend.postraveswiki.data.dto.reading

import kotlinx.serialization.Serializable

@Serializable
data class TimetableForSceneDto(
    val scene: SceneDto?,
    val performances: List<TimetablePerformanceDto>,
)
