package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.data.converters.UserConverters
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.repo.FollowableRepo
import jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface OtherUserRepo : FollowableRepo<UserShortDto> {
    fun findAll(): List<UserShortDto>
    fun deleteById(id: Long)
}

@Repository
class OtherUserRepoImpl(
    private val userConverters: UserConverters,
) : OtherUserRepo {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    override fun findAll(): List<UserShortDto> {
        return dsl
            .selectFrom(USER_PROFILE)
            .fetch()
            .map { convertToShortDto(it) }
            .toList()
    }

    override fun deleteById(id: Long) {
        dsl
            .selectFrom(USER_PROFILE)
            .where(USER_PROFILE.USER_PROFILE_ID.eq(id))
            .fetchOne()?.delete()
    }

    override fun findFollowableByPartOfName(userId: Long?, namePart: String): List<UserShortDto> {
        val results = dsl
            .selectFrom(USER_PROFILE)
            .where(DSL.lower(USER_PROFILE.USER_PROFILE_NAME).contains(namePart.lowercase()))
            .fetch()
            .map { convertToShortDto(it) }
            .toList()
        return results
    }

    override fun convertToShortDto(record: Record): UserShortDto {
        return userConverters.createShortDtoFromRecord(record.into(USER_PROFILE))
    }
}
