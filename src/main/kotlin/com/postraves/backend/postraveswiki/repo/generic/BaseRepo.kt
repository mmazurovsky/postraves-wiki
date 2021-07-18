package com.postraves.backend.postraveswiki.repo.generic

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto

interface BaseRepo<WRITEDTO : BaseWriteDto,
        SHORTDTO : BaseShortDto> {
    fun save(dto: WRITEDTO): SHORTDTO
    fun update(dto: WRITEDTO)
    fun findAll(): List<SHORTDTO>
}
