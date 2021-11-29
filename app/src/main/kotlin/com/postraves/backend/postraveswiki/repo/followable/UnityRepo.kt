package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.data.converters.UnityConverters
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.UnityWriteDto
import com.postraves.backend.postraveswiki.data.enum.EntityType
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.SaveException
import com.postraves.backend.postraveswiki.repo.BaseRepo
import com.postraves.backend.postraveswiki.repo.ByIdRepo
import com.postraves.backend.postraveswiki.repo.FollowableRepo
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import jooq.tables.records.UnityRecord
import jooq.tables.references.*
import org.jooq.*
import org.jooq.impl.DSL.lower
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface UnityRepo :
    BaseRepo<UnityWriteDto, UnityShortDto>,
    ByIdRepo<UnityFullDto, UnityShortDto>,
    FollowableRepo<UnityShortDto> {
    fun getArtistsOfUnity(userId: Long?, id: Long): List<ArtistShortDto>
    fun addArtistsToUnity(id: Long, artists: Set<Long>)
    fun removeArtistsFromUnity(id: Long, artists: Set<Long>)
}

@Repository
class UnityRepoImpl(
    private val unityConverters: UnityConverters,
    private val dateTimeProvider: DateTimeProvider,
    private val artistRepo: ArtistRepo
) :
    UnityRepo,
    AbstractRepo<UnityWriteDto, UnityFullDto, UnityShortDto, UnityRecord>(
        table = UNITY,
        entityType = EntityType.UNITY.nameString
    ) {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    val thisTable = UNITY
    val thisString = EntityType.UNITY.nameString
    val userFollowsTable = USER_FOLLOWS_UNITY

    override fun SelectJoinStep<Record>.joinLocation(): SelectOnConditionStep<Record> {
        return joinUnityLocation()
    }

    override fun SelectJoinStep<Record>.joinUserFollow(userId: Long): SelectOnConditionStep<Record> {
        return joinUnityUserFollow(userId)
    }

    override fun SelectJoinStep<Record>.joinOtherData(): SelectOnConditionStep<Record>? {
        return null
    }

    override fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record> {
        return this.where(thisTable.UNITY_ID.eq(id))
    }

    override fun convertToShortDto(record: Record): UnityShortDto {
        val isFollowed = record.into(userFollowsTable).userFollowsUnityUserProfileId != null
        return unityConverters.createShortDtoFromRecord(record.into(thisTable), record.into(COUNTRY), isFollowed)
    }

    override fun convertToFullDto(record: Record): UnityFullDto {
        val isFollowed = record.into(userFollowsTable).userFollowsUnityUserProfileId != null
        return unityConverters.createFullDtoFromRecord(record.into(thisTable), record.into(COUNTRY), isFollowed)
    }

    override fun SelectWhereStep<Record>.whereIdIsInIds(ids: Set<Long>): SelectConditionStep<Record> {
        return this.where(thisTable.UNITY_ID.`in`(ids))
    }

    override fun SelectWhereStep<Record>.whereNameIsLike(namePart: String): SelectConditionStep<Record> {
        return this.where(lower(thisTable.UNITY_NAME).contains(namePart.lowercase()))
    }

    override fun prepareRecordBeforeSaving(record: UnityRecord, dto: UnityWriteDto) {
        unityConverters.transferDataFromDtoToRecord(dto, record)
        record.unityCreatedDateTime = dateTimeProvider.getNow()
        record.unityUpdatedDateTime = dateTimeProvider.getNow()
    }

    override fun postSaveGetId(record: UnityRecord): Long {
        return record.unityId ?: throw SaveException(thisString, record.unityName ?: "NULL")
    }

    override fun preUpdateCheckId(dto: UnityWriteDto): Long {
        return dto.id ?: throw NotFoundException(thisString, dto.id.toString())
    }

    override fun prepareRecordBeforeUpdating(record: UnityRecord, dto: UnityWriteDto) {
        unityConverters.transferDataFromDtoToRecord(dto, record)
        record.unityUpdatedDateTime = dateTimeProvider.getNow()
    }

    override fun getArtistsOfUnity(userId: Long?, id: Long): List<ArtistShortDto> {
        return dsl
            .select()
            .from(UNITY_ARTIST)
            .leftOuterJoin(ARTIST).on(ARTIST.ARTIST_ID.eq(UNITY_ARTIST.UNITY_ARTIST_ARTIST_ID))
            .leftOuterJoin(COUNTRY).on(COUNTRY.COUNTRY_NAME.eq(ARTIST.ARTIST_COUNTRY_NAME))
            .apply { if (userId != null) joinArtistUserFollow(userId) }
            .where(UNITY_ARTIST.UNITY_ARTIST_UNITY_ID.eq(id))
            .fetch()
            .map { artistRepo.convertToShortDto(it) }
            .toList()
    }

    override fun addArtistsToUnity(id: Long, artists: Set<Long>) {
        artists.forEach {
            dsl
                .newRecord(UNITY_ARTIST)
                .apply {
                    this.unityArtistUnityId = id
                    this.unityArtistArtistId = it
                }
                .store()
        }
    }


    override fun removeArtistsFromUnity(id: Long, artists: Set<Long>) {
        artists.forEach {
            dsl
                .deleteFrom(UNITY_ARTIST)
                .where(UNITY_ARTIST.UNITY_ARTIST_UNITY_ID.eq(id), UNITY_ARTIST.UNITY_ARTIST_ARTIST_ID.eq(it))
                .execute()
        }
        updateUpdated
    }

    override fun updateUpdatedDateTimeInRecord(recordToUpdate: UnityRecord) {
        recordToUpdate.unityUpdatedDateTime = dateTimeProvider.getNow()
    }
}
