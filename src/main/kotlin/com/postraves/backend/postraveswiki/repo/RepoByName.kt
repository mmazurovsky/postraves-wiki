package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto

interface RepoByName<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO?
    fun deleteByName(name: String): FULLDTO?
}