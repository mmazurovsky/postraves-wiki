package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.BaseOperationsRepo
import com.postraves.backend.postraveswiki.data.dto.BaseDto
import com.postraves.backend.postraveswiki.data.projection.BaseProjection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface BaseMethodsService<DTO : BaseDto> {
    fun save(dto: DTO) : DTO
    fun update(dto: DTO) : DTO
    fun findById(id: Long) : DTO
    fun deleteById(id: Long)
    fun findAll(): List<DTO>
}

@Service
class BaseMethodsServiceImpl<DTO : BaseDto, PROJ : BaseProjection<DTO>,  DAO : BaseOperationsRepo<DTO, PROJ>>
    (
    private val baseMethodsDao: DAO
    ) : BaseMethodsService<DTO> {

    override fun findById(id: Long): DTO {
        val pro = baseMethodsDao.findById(id) ?: throw TODO()
        return pro.convertToDto()
    }

    override fun save(dto: DTO) : DTO {
        val pro = baseMethodsDao.save(dto) ?: throw TODO()
        return pro.convertToDto()
    }

    override fun update(dto: DTO) : DTO {
        val pro = baseMethodsDao.update(dto) ?: throw TODO()
        return pro.convertToDto()
    }

    override fun deleteById(id: Long) {
        baseMethodsDao.deleteById(id)
    }

    override fun findAll() : List<DTO> {
        val projs = baseMethodsDao.findAll()
        val dtos = projs.map { it.convertToDto() }.toList()
        return dtos
    }
}