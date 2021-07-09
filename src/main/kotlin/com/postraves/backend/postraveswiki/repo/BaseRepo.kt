package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import org.jooq.UpdatableRecord
import org.springframework.stereotype.Repository

interface BaseRepo<WRITEDTO : BaseWriteDto,
        SHORTDTO : BaseShortDto,
        FULLDTO : BaseFullDto> {
    fun save(dto: WRITEDTO): FULLDTO?
    fun update(dto: WRITEDTO): FULLDTO?
    fun findAll() : List<SHORTDTO>
}
