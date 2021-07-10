package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto

interface ByNameRepo<FULLDTO : BaseFullDto> {
    fun findByName(name: String): FULLDTO?
    fun deleteByName(name: String): FULLDTO?
}