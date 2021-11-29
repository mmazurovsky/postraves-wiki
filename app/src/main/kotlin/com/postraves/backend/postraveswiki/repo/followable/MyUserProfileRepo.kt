package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.converters.UserConverters
import com.postraves.backend.postraveswiki.data.dto.reading.*
import com.postraves.backend.postraveswiki.data.dto.writing.UserWriteDto
import com.postraves.backend.postraveswiki.exception.FollowingException
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.SaveException
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import jooq.tables.records.UserProfileRecord
import jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface MyUserProfileRepo {
    fun findMyProfileByAuthUid(authUid: String): UserFullDto?
    fun checkNicknameIsFree(nickname: String): Boolean
    fun save(dto: UserWriteDto, authUid: String): UserShortDto
    fun update(dto: UserWriteDto, userId: Long)
    fun deleteMyProfile(authUid: String)
    fun checkArtistIsFollowed(userId: Long, id: Long): Boolean
    fun checkEventIsFollowed(userId: Long, id: Long): Boolean
    fun checkPlaceIsFollowed(userId: Long, id: Long): Boolean
    fun checkUnityIsFollowed(userId: Long, id: Long): Boolean
    fun followArtist(userId: Long, id: Long)
    fun followEvent(userId: Long, id: Long)
    fun followPlace(userId: Long, id: Long)
    fun followUnity(userId: Long, id: Long)
    fun unfollowArtist(userId: Long, id: Long)
    fun unfollowEvent(userId: Long, id: Long)
    fun unfollowPlace(userId: Long, id: Long)
    fun unfollowUnity(userId: Long, id: Long)
    fun findMyFollowingArtists(userId: Long): List<ArtistShortDto>
    fun findMyFollowingEvents(userId: Long): List<EventShortDto>
    fun findMyFollowingPlaces(userId: Long): List<PlaceShortDto>
    fun findMyFollowingUnities(userId: Long): List<UnityShortDto>
}

@Repository
class MyUserProfileRepoImpl(
    private val dateTimeProvider: DateTimeProvider,
    private val artistRepo: ArtistRepo,
    private val eventRepo: EventRepo,
    private val placeRepo: PlaceRepo,
    private val unityRepo: UnityRepo,
    private val userConverters: UserConverters,
) : MyUserProfileRepo {

    @Qualifier("getDSLContext")
    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    private val thisTable = USER_PROFILE

    private fun findByAuthUidWithJoins(authUid: String): Record? {
        val record = dsl
            .selectFrom(
                thisTable
                    .leftOuterJoin(CITY).on(USER_PROFILE.USER_PROFILE_CITY_NAME.eq(CITY.CITY_NAME))
                    .leftOuterJoin(COUNTRY).on(CITY.CITY_COUNTRY_NAME.eq(COUNTRY.COUNTRY_NAME))
            )
            .where(USER_PROFILE.USER_PROFILE_AUTH_UID.eq(authUid))
            .fetchOne()
        return record
    }

    private fun findByIdWithoutJoins(userId: Long): UserProfileRecord {
        val record = dsl
            .selectFrom(thisTable)
            .where(USER_PROFILE.USER_PROFILE_ID.eq(userId))
            .fetchOne()
        return record ?: throw NotFoundException("User", userId.toString())
    }

    override fun findMyProfileByAuthUid(authUid: String): UserFullDto? {
        val user = findByAuthUidWithJoins(authUid)
        return if (user == null) null
        else userConverters.createFullDtoFromRecord(user.into(USER_PROFILE), user.into(CITY), user.into(COUNTRY))
    }

    override fun save(dto: UserWriteDto, authUid: String): UserShortDto {
        val userToSave = dsl.newRecord(thisTable)
        userConverters.transferDataFromDtoToRecord(dto, userToSave)
        userToSave.userProfileAuthUid = authUid
        userToSave.userProfileCreatedDateTime = dateTimeProvider.getNow()
        userToSave.userProfileUpdatedDateTime = dateTimeProvider.getNow()
        userToSave.store()
        val record = findByAuthUidWithJoins(authUid)
        return userConverters.createShortDtoFromRecord(
            record?.into(USER_PROFILE) ?: throw SaveException(
                "User",
                dto.name
            )
        )
    }

    override fun update(dto: UserWriteDto, userId: Long) {
        val userToUpdate = findByIdWithoutJoins(userId)
        userConverters.transferDataFromDtoToRecord(dto, userToUpdate)
        userToUpdate.userProfileUpdatedDateTime = dateTimeProvider.getNow()
        userToUpdate.update()
    }

    override fun deleteMyProfile(authUid: String) {
        dsl.fetchOne(thisTable, USER_PROFILE.USER_PROFILE_AUTH_UID.eq(authUid))
            ?.delete()
    }

    override fun followArtist(userId: Long, id: Long) {
        val userFollowArtist = dsl.newRecord(USER_FOLLOWS_ARTIST)
        userFollowArtist.userFollowsArtistArtistId = id
        userFollowArtist.userFollowsArtistUserProfileId = userId
        try {
            userFollowArtist.store()
        } catch (e: Exception) {
            logger.debug(e.toString())
            throw FollowingException(
                userId = userId,
                entity = "Artist",
                entityId = id.toString(),
                message = "can't follow"
            )
        }
    }

    override fun followEvent(userId: Long, id: Long) {
        val userFollowEvent = dsl.newRecord(USER_FOLLOWS_EVENT)
        userFollowEvent.userFollowsEventEventId = id
        userFollowEvent.userFollowsEventUserProfileId = userId
        try {
            userFollowEvent.store()
        } catch (e: Exception) {
            throw FollowingException(
                userId = userId,
                entity = "Event",
                entityId = id.toString(),
                message = "can't follow"
            )
        }
    }

    override fun followPlace(userId: Long, id: Long) {
        val userFollowPlace = dsl.newRecord(USER_FOLLOWS_PLACE)
        userFollowPlace.userFollowsPlacePlaceId = id
        userFollowPlace.userFollowsPlaceUserProfileId = userId
        try {
            userFollowPlace.store()
        } catch (e: Exception) {
            throw FollowingException(
                userId = userId,
                entity = "Place",
                entityId = id.toString(),
                message = "can't follow"
            )
        }
    }

    override fun followUnity(userId: Long, id: Long) {
        val userFollowUnity = dsl.newRecord(USER_FOLLOWS_UNITY)
        userFollowUnity.userFollowsUnityUnityId = id
        userFollowUnity.userFollowsUnityUserProfileId = userId
        try {
            userFollowUnity.store()
        } catch (e: Exception) {
            throw FollowingException(
                userId = userId,
                entity = "Unity",
                entityId = id.toString(),
                message = "can't follow"
            )
        }
    }

    override fun unfollowArtist(userId: Long, id: Long) {
        if (dsl.fetchOne(
                USER_FOLLOWS_ARTIST,
                USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_ARTIST_ID.eq(id),
                USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_USER_PROFILE_ID.eq(userId)
            )
                ?.delete() == null
        ) throw FollowingException(
            userId = userId,
            entity = "Artist",
            entityId = id.toString(),
            message = "can't unfollow"
        )
    }

    override fun unfollowEvent(userId: Long, id: Long) {
        if (dsl.fetchOne(
                USER_FOLLOWS_EVENT,
                USER_FOLLOWS_EVENT.USER_FOLLOWS_EVENT_EVENT_ID.eq(id),
                USER_FOLLOWS_EVENT.USER_FOLLOWS_EVENT_USER_PROFILE_ID.eq(userId)
            )
                ?.delete() == null
        ) throw FollowingException(
            userId = userId,
            entity = "Event",
            entityId = id.toString(),
            message = "can't unfollow"
        )
    }

    override fun unfollowPlace(userId: Long, id: Long) {
        if (dsl.fetchOne(
                USER_FOLLOWS_PLACE,
                USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_PLACE_ID.eq(id),
                USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_USER_PROFILE_ID.eq(userId)
            )
                ?.delete() == null
        ) throw FollowingException(
            userId = userId,
            entity = "Place",
            entityId = id.toString(),
            message = "can't unfollow"
        )

    }

    override fun unfollowUnity(userId: Long, id: Long) {
        if (dsl.fetchOne(
                USER_FOLLOWS_UNITY,
                USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_UNITY_ID.eq(id),
                USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_USER_PROFILE_ID.eq(userId)
            )
                ?.delete() == null
        ) throw FollowingException(
            userId = userId,
            entity = "Unity",
            entityId = id.toString(),
            message = "can't unfollow"
        )

    }

    override fun findMyFollowingArtists(userId: Long): List<ArtistShortDto> {
        return dsl
            .select().from(USER_FOLLOWS_ARTIST)
            .leftOuterJoin(ARTIST).on(USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_ARTIST_ID.eq(ARTIST.ARTIST_ID))
            .joinArtistLocation()
            .where(USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_USER_PROFILE_ID.eq(userId))
            .fetch()
            .map {
                artistRepo.convertToShortDto(it)
            }
            .toList()
    }

    override fun findMyFollowingEvents(userId: Long): List<EventShortDto> {
        return dsl
            .select()
            .from(USER_FOLLOWS_EVENT)
            .leftOuterJoin(EVENT).on(USER_FOLLOWS_EVENT.USER_FOLLOWS_EVENT_EVENT_ID.eq(EVENT.EVENT_ID))
            .joinEventLocation()
            .where(USER_FOLLOWS_EVENT.USER_FOLLOWS_EVENT_USER_PROFILE_ID.eq(userId))
            .fetch()
            .map {
                eventRepo.convertToShortDto(it)
            }
            .toList()
    }

    override fun findMyFollowingPlaces(userId: Long): List<PlaceShortDto> {
        return dsl
            .select()
            .from(USER_FOLLOWS_PLACE)
            .leftOuterJoin(PLACE).on(USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_PLACE_ID.eq(PLACE.PLACE_ID))
            .joinPlaceLocation()
            .where(USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_USER_PROFILE_ID.eq(userId))
            .fetch()
            .map {
                placeRepo.convertToShortDto(it)
            }
            .toList()
    }

    override fun findMyFollowingUnities(userId: Long): List<UnityShortDto> {
        return dsl
            .select()
            .from(USER_FOLLOWS_UNITY)
            .leftOuterJoin(UNITY).on(USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_UNITY_ID.eq(UNITY.UNITY_ID))
            .joinUnityLocation()
            .where(USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_USER_PROFILE_ID.eq(userId))
            .fetch()
            .map {
                unityRepo.convertToShortDto(it)
            }
            .toList()
    }

    override fun checkArtistIsFollowed(userId: Long, id: Long): Boolean {
        val association = dsl.fetchOne(
            USER_FOLLOWS_ARTIST,
            USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_ARTIST_ID.eq(id),
            USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_USER_PROFILE_ID.eq(userId)
        )

        return association != null
    }

    override fun checkEventIsFollowed(userId: Long, id: Long): Boolean {
        val association = dsl.fetchOne(
            USER_FOLLOWS_EVENT,
            USER_FOLLOWS_EVENT.USER_FOLLOWS_EVENT_EVENT_ID.eq(id),
            USER_FOLLOWS_EVENT.USER_FOLLOWS_EVENT_USER_PROFILE_ID.eq(userId)
        )

        return association != null
    }

    override fun checkPlaceIsFollowed(userId: Long, id: Long): Boolean {
        val association = dsl.fetchOne(
            USER_FOLLOWS_PLACE,
            USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_PLACE_ID.eq(id),
            USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_USER_PROFILE_ID.eq(userId)
        )

        return association != null
    }

    override fun checkUnityIsFollowed(userId: Long, id: Long): Boolean {
        val association = dsl.fetchOne(
            USER_FOLLOWS_UNITY,
            USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_UNITY_ID.eq(id),
            USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_USER_PROFILE_ID.eq(userId)
        )

        return association != null
    }

    override fun checkNicknameIsFree(nickname: String): Boolean {
        val foundProfile = dsl.fetchOne(thisTable, USER_PROFILE.USER_PROFILE_NAME.eq(nickname))
        return foundProfile == null
    }
}