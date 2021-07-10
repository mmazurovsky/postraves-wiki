package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.repo.ByIdRepo
import com.postraves.backend.postraveswiki.repo.RatingRepo
import org.springframework.stereotype.Service

interface ByIdService<FULLDTO : BaseFullDto> {
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long): FULLDTO
}

@Service
class ByIdServiceImpl<FULLDTO : BaseFullDto, REPO : ByIdRepo<FULLDTO>>
    (private val byIdRepo: REPO) :
    ByIdService<FULLDTO> {

    override fun findById(id: Long): FULLDTO {
        return byIdRepo.findById(id) ?: throw TODO()
    }

    override fun deleteById(id: Long) : FULLDTO {
        return byIdRepo.deleteById(id) ?: throw TODO()
    }
}