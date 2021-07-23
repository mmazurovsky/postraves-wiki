package com.postraves.backend.postraveswiki.repo.generic

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto

interface BaseRepo<WRITEDTO : BaseWriteDto,
        SHORTDTO : BaseShortDto> {
    fun save(dto: WRITEDTO): SHORTDTO
    fun update(dto: WRITEDTO)
    fun findAll(): List<SHORTDTO>
}

interface FindByName<SHORTDTO : BaseShortDto> {
    fun findByPartOfName(namePart: String): List<SHORTDTO>
}

interface ByIdRepo<FULLDTO : BaseFullDto, SHORTDTO: BaseShortDto> {
    fun findById(id: Long): FULLDTO
    fun findByIdForUser(authUid: String, id: Long): FULLDTO
    fun deleteById(id: Long)
    fun findListByIds(ids: Set<Long>): List<SHORTDTO>
}

interface ByNameRepo<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO
    fun deleteByName(name: String)
}


