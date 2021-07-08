package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import org.jooq.UpdatableRecord
import org.springframework.stereotype.Repository

interface BaseOperationsRepo<RECORD : UpdatableRecord<RECORD>,
        WRITEDTO : BaseWriteDto<RECORD>,
        SHORTDTO : BaseShortDto,
        FULLDTO : BaseFullDto> {
    fun findById(id: Long): FULLDTO?
    fun save(dto: WRITEDTO): FULLDTO?
    fun update(dto: WRITEDTO): FULLDTO?
    fun deleteById(id: Long): FULLDTO?
}

interface SaveRepo<RECORD : UpdatableRecord<RECORD>,
        WRITEDTO : BaseWriteDto<RECORD>> {
    fun save(dto: WRITEDTO): Long
}

@Repository
class SaveRepoImpl<RECORD : UpdatableRecord<RECORD>,
        WRITEDTO : BaseWriteDto<RECORD>> : SaveRepo<RECORD, WRITEDTO> {
    override fun save(dto: WRITEDTO): Long {
        val record = dto.convertToDbRecord()
        val recordId = record.store().toLong()
        return recordId
    }
}