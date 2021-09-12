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
    val imageLink: String?,
    val priority: Int,
) : BaseWriteDto, BaseShortDto, BaseFullDto
