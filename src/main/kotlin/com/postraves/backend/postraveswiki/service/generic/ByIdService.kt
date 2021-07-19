package com.postraves.backend.postraveswiki.service.generic

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto

interface ByIdService<FULLDTO : BaseFullDto> {
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long)
}
