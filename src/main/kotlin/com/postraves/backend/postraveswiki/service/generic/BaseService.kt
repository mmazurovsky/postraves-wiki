package com.postraves.backend.postraveswiki.service.generic

import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto

interface BaseService<WRITEDTO : BaseWriteDto, SHORTDTO : BaseShortDto> {
    fun save(dto: WRITEDTO) : SHORTDTO
    fun update(dto: WRITEDTO)
    fun findAll(): List<SHORTDTO>
}
