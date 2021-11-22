package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.data.dto.CoordinateDto
import jooq.tables.records.PlaceRecord
import kotlinx.serialization.Serializable

@Serializable
data class PlaceWriteDto(
    val id: Long? = null,
    val name: String,
    val cityName: String,
    val streetAddress: String,
    val coordinate: CoordinateDto,
    val imageLink: String? = null,
    val soundcloudUsername: String? = null,
    val instagramUsername: String? = null,
    val about: String? = null,
    val isJustCity: Boolean = false,
) : BaseWriteDto