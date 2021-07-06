package com.postraves.backend.postraveswiki.repo

interface BaseOperationsRepo<DTO, PROJ> {
    fun findById(id: Long) : PROJ?
    fun save(dto: DTO) : PROJ?
    fun update(dto: DTO) : PROJ?
    fun deleteById(id: Long)
    fun findAll(): List<PROJ>
}