package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.BaseOperationsRepo
import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import org.springframework.stereotype.Service

interface BaseMethodsService<SHORTDTO : BaseShortDto, FULLDTO : BaseFullDto> {
    fun save(dto: FULLDTO) : FULLDTO
    fun update(dto: FULLDTO) : FULLDTO
    fun findById(id: Long) : FULLDTO
    fun deleteById(id: Long)
    fun findAll(): List<SHORTDTO>
}

@Service
class BaseMethodsServiceImpl<SHORTDTO : BaseShortDto, FULLDTO : BaseFullDto,  DAO : BaseOperationsRepo<SHORTDTO, FULLDTO>>
    (
    private val baseMethodsDao: DAO
    ) : BaseMethodsService<SHORTDTO, FULLDTO> {

    override fun findById(id: Long): FULLDTO {
        return baseMethodsDao.findById(id) ?: throw TODO()
    }

    override fun save(dto: FULLDTO): FULLDTO {
        TODO("Not yet implemented")
    }

    override fun update(dto: FULLDTO): FULLDTO {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<SHORTDTO> {
        TODO("Not yet implemented")
    }
}