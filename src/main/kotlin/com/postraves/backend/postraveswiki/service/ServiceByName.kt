package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.repo.ByNameRepo
import org.springframework.stereotype.Service

interface ServiceByName<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO
    fun deleteByName(name: String): FULLDTO
}

@Service
class ServiceByNameImpl<
        FULLDTO : BaseFullDto,
        REPO : ByNameRepo<FULLDTO>>
    (
    private val repoByName: REPO
) : ServiceByName<FULLDTO> {
    override fun findByName(name: String): FULLDTO {
        return repoByName.findByName(name) ?: throw TODO()
    }

    override fun deleteByName(name: String): FULLDTO {
        return repoByName.deleteByName(name) ?: throw TODO()
    }

}