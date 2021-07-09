package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.BaseRepo
import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import org.jooq.UpdatableRecord
import org.springframework.stereotype.Service

interface BaseService<WRITEDTO : BaseWriteDto, SHORTDTO : BaseShortDto, FULLDTO : BaseFullDto> {
    fun save(dto: WRITEDTO): FULLDTO
    fun update(dto: WRITEDTO): FULLDTO
//    fun findById(id: Long): FULLDTO
//    fun deleteById(id: Long)
    fun findAll(): List<SHORTDTO>
}

class BaseServiceImpl<WRITEDTO : BaseWriteDto,
        SHORTDTO : BaseShortDto,
        FULLDTO : BaseFullDto,
        REPO : BaseRepo<WRITEDTO, SHORTDTO, FULLDTO>>
    (
    private val baseRepo: REPO
) : BaseService<WRITEDTO, SHORTDTO, FULLDTO> {

    override fun save(dto: WRITEDTO): FULLDTO {
        return baseRepo.save(dto) ?: throw TODO()
    }

    override fun update(dto: WRITEDTO): FULLDTO {
        return baseRepo.update(dto) ?: throw TODO()
    }

    override fun findAll(): List<SHORTDTO> {
        return baseRepo.findAll()
    }
}