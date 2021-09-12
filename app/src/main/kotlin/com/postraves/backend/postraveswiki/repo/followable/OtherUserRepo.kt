package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.data.converters.UserConverters
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.repo.FollowableRepo
import jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface OtherUserRepo : FollowableRepo<UserShortDto>

@Repository
class OtherUserRepoImpl(
    private val userConverters: UserConverters,
) : OtherUserRepo {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    override fun findFollowableByPartOfName(authUid: String?, namePart: String): List<UserShortDto> {
        val results = dsl
            .selectFrom(USER_PROFILE)
            .where(DSL.lower(USER_PROFILE.NAME).contains(namePart.lowercase()))
            .fetch()
            .map { convertToShortDto(it) }
            .toList()
        return results
    }

    override fun convertToShortDto(record: Record): UserShortDto {
        return userConverters.createShortDtoFromRecord(record.into(USER_PROFILE))
    }
}
