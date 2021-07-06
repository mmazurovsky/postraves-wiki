package com.postraves.backend.postraveswiki.data.projection

import com.postraves.backend.postraveswiki.data.dto.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseDto

interface BaseProjection<T : BaseDto> {
    fun convertToDto(): T
}