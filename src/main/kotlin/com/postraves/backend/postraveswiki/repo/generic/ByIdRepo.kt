package com.postraves.backend.postraveswiki.repo.generic

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto

interface ByIdRepo<FULLDTO : BaseFullDto, SHORTDTO: BaseShortDto> {
    fun findById(id: Long): FULLDTO
    fun findByIdForUser(authUid: String, id: Long): FULLDTO
    fun deleteById(id: Long)
    fun findListByIds(ids: Set<Long>): List<SHORTDTO>
}