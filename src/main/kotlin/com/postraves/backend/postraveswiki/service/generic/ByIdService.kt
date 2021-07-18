package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.repo.generic.ByIdRepo

interface ByIdService<FULLDTO : BaseFullDto> {
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long)
}

class ByIdServiceImpl<FULLDTO : BaseFullDto, REPO : ByIdRepo<FULLDTO>>
    (private val byIdRepo: REPO) :
    ByIdService<FULLDTO> {

    override fun findById(id: Long): FULLDTO {
        return byIdRepo.findById(id) ?: throw TODO()
    }

    override fun deleteById(id: Long) {
        byIdRepo.deleteById(id)
    }
}