package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.generic.BaseRepo
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto

interface BaseService<WRITEDTO : BaseWriteDto, SHORTDTO : BaseShortDto> {
    fun save(dto: WRITEDTO) : SHORTDTO
    fun update(dto: WRITEDTO)
    fun findAll(): List<SHORTDTO>
}

class BaseServiceImpl<WRITEDTO : BaseWriteDto,
        SHORTDTO : BaseShortDto,
        REPO : BaseRepo<WRITEDTO, SHORTDTO>>
    (
    private val baseRepo: REPO
) : BaseService<WRITEDTO, SHORTDTO> {

    override fun save(dto: WRITEDTO): SHORTDTO {
        return baseRepo.save(dto)
    }

    override fun update(dto: WRITEDTO) {
        return baseRepo.update(dto)
    }

    override fun findAll(): List<SHORTDTO> {
        return baseRepo.findAll()
    }
}