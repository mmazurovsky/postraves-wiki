package com.postraves.backend.postraveswiki.service.generic

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto

interface ByIdService<FULLDTO : BaseFullDto, SHORTDTO : BaseShortDto> {
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long)
//    fun findListByIds(ids: Set<Long>): List<SHORTDTO>
}
