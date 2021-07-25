package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface OtherUserRepo : FindByNameRepo<UserShortDto>

@Repository
class OtherUserRepoImpl : OtherUserRepo {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    override fun findByPartOfName(namePart: String): List<UserShortDto> {
        val results = dsl
            .selectFrom(USER_PROFILE)
            .where(DSL.lower(USER_PROFILE.NAME).contains(namePart.lowercase()))
            .fetch()
            .map { UserShortDto.createOutOfDbRecords(it.into(USER_PROFILE)) }
            .toList()
        return results
    }

}