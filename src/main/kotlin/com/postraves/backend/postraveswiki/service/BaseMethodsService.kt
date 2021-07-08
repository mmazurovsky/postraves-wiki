package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.BaseOperationsRepo
import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import org.jooq.UpdatableRecord
import org.springframework.stereotype.Service

interface BaseMethodsService<RECORD : UpdatableRecord<RECORD>, WRITEDTO : BaseWriteDto<RECORD>, SHORTDTO : BaseShortDto, FULLDTO : BaseFullDto> {
    fun save(dto: WRITEDTO): FULLDTO
    fun update(dto: WRITEDTO): FULLDTO
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long)
}

@Service
class BaseMethodsServiceImpl<RECORD : UpdatableRecord<RECORD>,
        WRITEDTO : BaseWriteDto<RECORD>,
        SHORTDTO : BaseShortDto,
        FULLDTO : BaseFullDto,
        REPO : BaseOperationsRepo<RECORD, WRITEDTO, SHORTDTO, FULLDTO>>
    (
    private val baseRepo: REPO
) : BaseMethodsService<RECORD, WRITEDTO, SHORTDTO, FULLDTO> {

    override fun findById(id: Long): FULLDTO {
        return baseRepo.findById(id) ?: throw TODO()
    }

    override fun save(dto: WRITEDTO): FULLDTO {
        TODO("Not yet implemented")
    }

    override fun update(dto: WRITEDTO): FULLDTO {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) {
        TODO("Not yet implemented")
    }
}