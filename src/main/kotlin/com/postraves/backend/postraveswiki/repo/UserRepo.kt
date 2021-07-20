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
    fun findMyProfile(authUid: String): UserFullDto?
    fun save(dto: UserWriteDto, authUid: String): UserShortDto
    fun update(dto: UserWriteDto, authUid: String)
    fun deleteMyProfile(authUid: String)
    fun followArtist(userAuthUid: String, id: Long)
    fun unfollowArtist(userAuthUid: String, id: Long)
    fun findMyFollowsArtist(authUid: String): List<ArtistShortDto>
    fun checkIsFollowedArtist(userAuthUid: String, id: Long): Boolean
}

@Repository
class UserRepoImpl(
    private val dslContextConfig: JooqDSLContextConfig,
    private val artistRepo: ArtistRepo
) : UserRepo {

    private val dsl: DSLContext by lazy { dslContextConfig.getDSLContext() }

    private fun findByAuthUidWithJoins(authUid: String): Record? {
        val record = dsl
            .selectFrom(
                USER_PROFILE
                    .leftOuterJoin(CITY).on(USER_PROFILE.CITY_NAME.eq(CITY.NAME))
                    .leftOuterJoin(COUNTRY).on(CITY.COUNTRY_NAME.eq(COUNTRY.NAME))
            )
            .where(USER_PROFILE.AUTH_UID.eq(authUid))
            .fetchOne()
        return record
    }

    private fun findByAuthUidWithoutJoins(authUid: String): UserProfileRecord {
        val record = dsl
            .selectFrom(USER_PROFILE)
            .where(USER_PROFILE.AUTH_UID.eq(authUid))
            .fetchOne()
        return record ?: throw TODO()
    }

    override fun findMyProfile(authUid: String): UserFullDto? {
        val user = findByAuthUidWithJoins(authUid)
        return if (user == null) null
        else UserFullDto.createOutOfDbRecords(user.into(USER_PROFILE), user.into(CITY), user.into(COUNTRY))
    }

    override fun save(dto: UserWriteDto, authUid: String): UserShortDto {
        val userToSave = dsl.newRecord(USER_PROFILE)
        dto.transferDataToDbRecord(userToSave)
        userToSave.authUid = authUid
        userToSave.createdDateTime = OffsetDateTime.now()
        userToSave.overallFollowersCount = 0
        userToSave.store()
        val record = findByAuthUidWithJoins(authUid)
        return UserShortDto.createOutOfDbRecords(record?.into(USER_PROFILE) ?: throw TODO())
    }

    override fun update(dto: UserWriteDto, authUid: String) {
        val userToUpdate = findByAuthUidWithoutJoins(authUid)
        dto.transferDataToDbRecord(userToUpdate)
        userToUpdate.update()
    }

    override fun deleteMyProfile(authUid: String) {
        dsl.fetchOne(USER_PROFILE, USER_PROFILE.AUTH_UID.eq(authUid))
            ?.delete()
    }

    override fun followArtist(userAuthUid: String, id: Long) {
        // checking that artist exists
        artistRepo.findById(id)
        val userFollowArtist = dsl.newRecord(USER_FOLLOWS_ARTIST)
        userFollowArtist.artistId = id
        userFollowArtist.userProfileUid = userAuthUid
        userFollowArtist.store()
//        userFollowArtist.userProfileId = userAuthUid
    }

    override fun unfollowArtist(userAuthUid: String, id: Long) {
        artistRepo.findById(id)
        //todo
        if (dsl.fetchOne(
                USER_FOLLOWS_ARTIST,
                USER_FOLLOWS_ARTIST.ARTIST_ID.eq(id),
                USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(userAuthUid)
            )
                ?.delete() == null
        ) throw TODO()
    }

    override fun findMyFollowsArtist(authUid: String): List<ArtistShortDto> {
        //todo
        return dsl
            .selectFrom(
                USER_FOLLOWS_ARTIST
                    .leftOuterJoin(ARTIST).on(USER_FOLLOWS_ARTIST.ARTIST_ID.eq(ARTIST.ID))
                    .leftOuterJoin(COUNTRY).on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME))
            )
            .where(USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(authUid))
            .fetch()
            .map { ArtistShortDto.createOutOfDbRecords(it.into(ARTIST), it.into(COUNTRY)) }
            .toList()
    }

    override fun checkIsFollowedArtist(userAuthUid: String, id: Long): Boolean {
        val record = dsl
            .selectFrom(
                USER_FOLLOWS_ARTIST
                    .leftOuterJoin(ARTIST).on(USER_FOLLOWS_ARTIST.ARTIST_ID.eq(ARTIST.ID))
                    .leftOuterJoin(COUNTRY).on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME))
            )
            .where(USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(userAuthUid))
            .fetchOne()

        return if (record == null) false else true
    }

}