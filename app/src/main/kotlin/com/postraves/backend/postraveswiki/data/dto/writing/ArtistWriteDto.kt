package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import jooq.tables.records.ArtistRecord
import kotlinx.serialization.Serializable

@Serializable
data class ArtistWriteDto(
    val id: Long? = null,
    val name: String,
    val imageLink : String? = null,
    val countryName: String? = null,
    val soundcloudUsername: String? = null,
    val instagramUsername: String? = null,
    val about: String? = null,
) : BaseWriteDto