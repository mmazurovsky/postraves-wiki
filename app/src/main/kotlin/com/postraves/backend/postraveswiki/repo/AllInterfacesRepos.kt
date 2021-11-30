package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.*
import org.jooq.Record
import org.jooq.SelectJoinStep
import org.jooq.SelectOnConditionStep

interface BaseRepo<WRITEDTO : BaseWriteDto,
        SHORTDTO : BaseShortDto> {
    fun save(dto: WRITEDTO): SHORTDTO
    fun update(dto: WRITEDTO)
    fun findAll(): List<SHORTDTO>
}

interface FollowableRepo<SHORTDTO : BaseShortDto> {
    fun findFollowableByPartOfName(userId: Long?, namePart: String): List<SHORTDTO>
    fun convertToShortDto(record: Record): SHORTDTO
}

interface ByIdRepo<FULLDTO : FollowableFullDto<FULLDTO>, SHORTDTO : FollowableShortDto<SHORTDTO>>{
    fun findById(userId: Long?, id: Long): FULLDTO
    fun updateUpdatedDateTime(id: Long)
    fun deleteById(id: Long)
    fun findListByIds(userId: Long?, ids: Set<Long>): List<SHORTDTO>
}

interface ByNameRepo<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO
    fun deleteByName(name: String)
    fun findByPartOfName(namePart: String): List<FULLDTO>
}


