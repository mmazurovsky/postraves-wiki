package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto

interface BaseRatingRepo<FULLDTO : BaseFullDto> {
    fun findById(id: Long): FULLDTO?
    fun deleteById(id: Long): FULLDTO?
}
