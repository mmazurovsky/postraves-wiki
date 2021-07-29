package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.exception.FollowingException
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.SaveException
import jooq.tables.records.UserProfileRecord
import jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface MyUserProfileRepo {
    fun findMyProfile(authUid: String): Pair<UserFullDto?, String>
    fun save(dto: UserWriteDto, authUid: String): UserShortDto
    fun update(dto: UserWriteDto, authUid: String)
    fun deleteMyProfile(authUid: String)
    fun followArtist(userAuthUid: String, id: Long)
    fun unfollowArtist(userAuthUid: String, id: Long)
    fun findMyFollowsArtist(authUid: String): List<ArtistShortDto>
    fun checkArtistIsFollowed(id: Long, authUid: String): Boolean
}

@Repository
class MyUserProfileRepoImpl(
    private val artistRepo: ArtistRepo
) : MyUserProfileRepo {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

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
        return record ?: throw NotFoundException("User", authUid)
    }

    override fun findMyProfile(authUid: String): Pair<UserFullDto?, String> {
        val user = findByAuthUidWithJoins(authUid)
        return if (user == null) null to authUid
        else UserFullDto.createOutOfDbRecords(user.into(USER_PROFILE), user.into(CITY), user.into(COUNTRY)) to authUid
    }

    override fun save(dto: UserWriteDto, authUid: String): UserShortDto {
        val userToSave = dsl.newRecord(USER_PROFILE)
        dto.transferDataToDbRecord(userToSave)
        userToSave.authUid = authUid
        userToSave.createdDateTime = OffsetDateTime.now()
        userToSave.store()
        val record = findByAuthUidWithJoins(authUid)
        return UserShortDto.createOutOfDbRecords(record?.into(USER_PROFILE) ?: throw SaveException("User", dto.name))
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
        artistRepo.findById(userAuthUid, id)
        val userFollowArtist = dsl.newRecord(USER_FOLLOWS_ARTIST)
        userFollowArtist.artistId = id
        userFollowArtist.userProfileUid = userAuthUid
        try {
            userFollowArtist.store()
        } catch (e: Exception) {
            throw FollowingException(userId = userAuthUid, entity = "Artist", entityId = id.toString(), message = "can't follow")
        }
    }

    override fun unfollowArtist(userAuthUid: String, id: Long) {
        artistRepo.findById(userAuthUid, id)
        if (dsl.fetchOne(
                USER_FOLLOWS_ARTIST,
                USER_FOLLOWS_ARTIST.ARTIST_ID.eq(id),
                USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(userAuthUid)
            )
                ?.delete() == null
        ) throw FollowingException(userId = userAuthUid, entity = "Artist", entityId = id.toString(), message = "can't unfollow")
    }

    override fun findMyFollowsArtist(authUid: String): List<ArtistShortDto> {
        return dsl
            .selectFrom(
                USER_FOLLOWS_ARTIST
                    .leftOuterJoin(ARTIST).on(USER_FOLLOWS_ARTIST.ARTIST_ID.eq(ARTIST.ID))
                    .leftOuterJoin(COUNTRY).on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME))
            )
            .where(USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(authUid))
            .fetch()
            .map { ArtistShortDto.createOutOfDbRecords(it.into(ARTIST), it.into(COUNTRY), true) }
            .toList()
    }

    override fun checkArtistIsFollowed(id: Long, authUid: String): Boolean {
        val association = dsl.fetchOne(
            USER_FOLLOWS_ARTIST,
            USER_FOLLOWS_ARTIST.ARTIST_ID.eq(id),
            USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(authUid)
        )

        return association != null
    }
}