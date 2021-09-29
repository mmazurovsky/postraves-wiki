package com.postraves.backend.postraveswiki.data.dto.writing

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import jooq.tables.records.UnityRecord
import kotlinx.serialization.Serializable

@Serializable
data class UnityWriteDto(
    val id: Long? = null,
    val name: String,
    val imageLink : String? = null,
    val countryName: String? = null,
    val soundcloudLink: String? = null,
    val instagramLink: String? = null,
    val bandcampLink: String? = null,
    val about: String? = null,
) : BaseWriteDto