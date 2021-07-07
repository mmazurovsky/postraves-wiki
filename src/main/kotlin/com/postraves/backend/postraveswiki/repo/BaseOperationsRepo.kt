package com.postraves.backend.postraveswiki.repo

interface BaseOperationsRepo<SHORTDTO, FULLDTO> {
    fun findById(id: Long) : FULLDTO?
    fun save(dto: FULLDTO) : FULLDTO?
    fun update(dto: FULLDTO) : FULLDTO?
    fun deleteById(id: Long) : FULLDTO?
    fun findAll(): List<SHORTDTO>
}