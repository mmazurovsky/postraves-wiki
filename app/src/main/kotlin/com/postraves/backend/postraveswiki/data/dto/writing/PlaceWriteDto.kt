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
    val imageLink : String? = null,
    val soundcloudLink: String? = null,
    val instagramLink: String? = null,
    val about: String? = null,
) : BaseWriteDto