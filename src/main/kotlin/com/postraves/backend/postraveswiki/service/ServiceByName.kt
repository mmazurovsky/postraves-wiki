package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.repo.BaseRepo
import com.postraves.backend.postraveswiki.repo.RepoByName
import org.jooq.UpdatableRecord
import org.springframework.stereotype.Service

interface ServiceByName<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO
    fun deleteByName(name: String): FULLDTO
}

@Service
class ServiceByNameImpl<
        FULLDTO : BaseFullDto,
        REPO : RepoByName<FULLDTO>>
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