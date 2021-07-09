package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.repo.BaseRatingRepo
import org.jooq.UpdatableRecord
import org.springframework.stereotype.Service

interface BaseRatingService<FULLDTO : BaseFullDto> {
    fun findById(id: Long): FULLDTO
    fun deleteById(id: Long): FULLDTO
}

@Service
class BaseRatingServiceImpl<
        FULLDTO : BaseFullDto,
        REPO : BaseRatingRepo<FULLDTO>>
    (
    private val baseRepo: REPO
) : BaseRatingService<FULLDTO> {

    override fun findById(id: Long): FULLDTO {
        return baseRepo.findById(id) ?: throw TODO()
    }

    override fun deleteById(id: Long) : FULLDTO {
        return baseRepo.deleteById(id) ?: throw TODO()
    }
}