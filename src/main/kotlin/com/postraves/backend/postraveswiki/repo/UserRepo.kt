package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import jooq.tables.records.UserProfileRecord
import jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface UserRepo {
    fun findMyProfile(authUid: String): UserFullDto
    fun save(dto: UserWriteDto, authUid: String): UserShortDto
    fun update(dto: UserWriteDto, authUid: String)
    fun deleteMyProfile(authUid: String)
    fun followArtist(userAuthUid: String, id: Long)
    fun unfollowArtist(userAuthUid: String, id: Long)
    fun findMyFollowsArtist(authUid: String): List<ArtistShortDto>
    fun findByAuthUid(authUid: String): UserFullDto?
}

@Repository
class UserRepoImpl(
    private val dslContextConfig: JooqDSLContextConfig,
    private val artistRepo: ArtistRepo
) : UserRepo {

    private val dsl: DSLContext by lazy { dslContextConfig.getDSLContext() }

    private fun findByAuthUidWithJoins(authUid: String): Record {
        val record = dsl
            .selectFrom(
                USER_PROFILE
                    .leftOuterJoin(CITY).on(USER_PROFILE.CITY_NAME.eq(CITY.NAME))
                    .leftOuterJoin(COUNTRY).on(CITY.COUNTRY_NAME.eq(COUNTRY.NAME))
            )
            .where(USER_PROFILE.AUTH_UID.eq(authUid))
            .fetchOne()
        return record ?: throw TODO()
    }

    private fun findByAuthUidWithoutJoins(authUid: String): UserProfileRecord {
        val record = dsl
            .selectFrom(USER_PROFILE)
            .where(USER_PROFILE.AUTH_UID.eq(authUid))
            .fetchOne()
        return record ?: throw TODO()
    }

    override fun findMyProfile(authUid: String): UserFullDto {
        val user = findByAuthUidWithJoins(authUid)
        return UserFullDto.createOutOfDbRecords(user.into(USER_PROFILE), user.into(CITY), user.into(COUNTRY))
    }

    override fun save(dto: UserWriteDto, authUid: String): UserShortDto {
        val userToSave = dsl.newRecord(USER_PROFILE)
        dto.transferDataToDbRecord(userToSave)
        userToSave.authUid = authUid
        userToSave.createdDateTime = OffsetDateTime.now()
        userToSave.overallFollowersCount = 0
        userToSave.store()
        val record = findByAuthUidWithJoins(authUid)
        return UserShortDto.createOutOfDbRecords(record.into(USER_PROFILE))
    }

    override fun update(dto: UserWriteDto, authUid: String) {
        val userToUpdate = findByAuthUidWithoutJoins(authUid)
        dto.transferDataToDbRecord(userToUpdate)
        userToUpdate.update()
    }

    override fun deleteMyProfile(authUid: String) {
        if (dsl.fetchOne(USER_PROFILE, USER_PROFILE.AUTH_UID.eq(authUid))
                ?.delete() == null
        ) throw TODO()
    }

    override fun followArtist(userAuthUid: String, id: Long) {
        // checking that artist exists
        artistRepo.findById(id)
        val userFollowArtist = dsl.newRecord(USER_BOOKMARKS_ARTIST)
        userFollowArtist.artistId = id
        //TODO
//        userFollowArtist.userProfileId = userAuthUid
    }

    override fun unfollowArtist(userAuthUid: String, id: Long) {
        artistRepo.findById(id)
        //todo
        if (dsl.fetchOne(
                USER_BOOKMARKS_ARTIST,
                USER_BOOKMARKS_ARTIST.ARTIST_ID.eq(id),
                USER_BOOKMARKS_ARTIST.USER_PROFILE_ID.eq(1)
            )
                ?.delete() == null
        ) throw TODO()
    }

    override fun findMyFollowsArtist(authUid: String): List<ArtistShortDto> {
        //todo
        return dsl
            .selectFrom(
                USER_BOOKMARKS_ARTIST
                    .leftOuterJoin(ARTIST).on(USER_BOOKMARKS_ARTIST.ARTIST_ID.eq(ARTIST.ID))
                    .leftOuterJoin(COUNTRY).on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME))
            )
            .where(USER_BOOKMARKS_ARTIST.USER_PROFILE_ID.eq(1))
            .fetch()
            .map { ArtistShortDto.createOutOfDbRecords(it.into(ARTIST), it.into(COUNTRY)) }
            .toList()
    }

    override fun findByAuthUid(authUid: String): UserFullDto? {
        return try {
            val recordWithJoins = findByAuthUidWithJoins(authUid)
            UserFullDto.createOutOfDbRecords(
                recordWithJoins.into(USER_PROFILE),
                recordWithJoins.into(CITY),
                recordWithJoins.into(COUNTRY)
            )
        } catch (e: Exception) {
            null
        }
    }
}