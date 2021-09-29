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
    fun findFollowableByPartOfName(authUid: String?, namePart: String): List<SHORTDTO>
    fun convertToShortDto(record: Record): SHORTDTO
}

interface ByIdRepo<FULLDTO : FollowableFullDto<FULLDTO>, SHORTDTO : FollowableShortDto<SHORTDTO>>{
    fun findById(authUid: String?, id: Long): FULLDTO
    fun deleteById(id: Long)
    fun findListByIds(authUid: String?, ids: Set<Long>): List<SHORTDTO>
}

interface ByNameRepo<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO
    fun deleteByName(name: String)
    fun findByPartOfName(namePart: String): List<FULLDTO>
}


