package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.*

interface BaseRepo<WRITEDTO : BaseWriteDto,
        SHORTDTO : BaseShortDto> {
    fun save(dto: WRITEDTO): SHORTDTO
    fun update(dto: WRITEDTO)
    fun findAll(): List<SHORTDTO>
}

interface FindByNameRepo<SHORTDTO : BaseShortDto> {
    fun findByPartOfName(namePart: String): List<SHORTDTO>
}

interface ByIdRepo<FULLDTO : BaseFullDtoWithId, SHORTDTO: BaseShortDtoWithId> {
    fun findById(id: Long): FULLDTO
    fun findByIdForUser(authUid: String, id: Long): FULLDTO
    fun deleteById(id: Long)
    fun findListByIds(ids: Set<Long>): List<SHORTDTO>
}

interface ByNameRepo<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO
    fun deleteByName(name: String)
}


