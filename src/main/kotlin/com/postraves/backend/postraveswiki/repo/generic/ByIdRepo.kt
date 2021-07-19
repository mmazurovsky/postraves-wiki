package com.postraves.backend.postraveswiki.repo.generic

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto

interface ByIdRepo<FULLDTO : BaseFullDto> {
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long)
}