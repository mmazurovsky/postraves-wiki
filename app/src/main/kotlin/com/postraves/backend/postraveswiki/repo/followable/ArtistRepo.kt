package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.data.converters.ArtistConverters
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.UnityShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.data.enum.EntityType
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.SaveException
import com.postraves.backend.postraveswiki.repo.BaseRepo
import com.postraves.backend.postraveswiki.repo.ByIdRepo
import com.postraves.backend.postraveswiki.repo.FollowableRepo
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import jooq.tables.records.ArtistRecord
import jooq.tables.references.*
import org.jooq.*
import org.jooq.impl.DSL.lower
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface ArtistRepo :
    BaseRepo<ArtistWriteDto, ArtistShortDto>,
    ByIdRepo<ArtistFullDto, ArtistShortDto>,
    FollowableRepo<ArtistShortDto> {
    fun getUnitiesOfArtist(authUid: String?, id: Long): List<UnityShortDto>
    }

@Repository
class ArtistRepoImpl(
    private val artistConverters: ArtistConverters,
    private val dateTimeProvider: DateTimeProvider,
    @Lazy
    private val unityRepo: UnityRepo,
    ) :
    ArtistRepo,
    AbstractRepo<ArtistWriteDto, ArtistFullDto, ArtistShortDto, ArtistRecord>(
        table = ARTIST,
        entityType = EntityType.ARTIST.nameString
    ) {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    override fun SelectJoinStep<Record>.joinLocation(): SelectOnConditionStep<Record> {
        return this.leftOuterJoin(COUNTRY).on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME))
    }

    override fun SelectJoinStep<Record>.joinUserFollow(authUid: String): SelectOnConditionStep<Record> {
        return this.leftOuterJoin(USER_FOLLOWS_ARTIST)
            .on(ARTIST.ID.eq(USER_FOLLOWS_ARTIST.ARTIST_ID), USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(authUid))
    }

    override fun SelectJoinStep<Record>.joinOtherData(): SelectOnConditionStep<Record>? {
        return null
    }

    override fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record> {
        return this.where(ARTIST.ID.eq(id))
    }

    private fun SelectJoinStep<Record>.joinUserFollowUnity(authUid: String): SelectOnConditionStep<Record> {
        return this.leftOuterJoin(USER_FOLLOWS_UNITY)
            .on(UNITY.ID.eq(USER_FOLLOWS_UNITY.UNITY_ID), USER_FOLLOWS_UNITY.USER_PROFILE_UID.eq(authUid))
    }

    override fun getUnitiesOfArtist(authUid: String?, id: Long): List<UnityShortDto> {
        return dsl
            .select()
            .from(UNITY_ARTIST)
            .leftOuterJoin(UNITY).on(UNITY.ID.eq(UNITY_ARTIST.UNITY_ID))
            .leftOuterJoin(COUNTRY).on(COUNTRY.NAME.eq(UNITY.COUNTRY_NAME))
            .apply { if (authUid != null) joinUserFollowUnity(authUid) }
            .where(UNITY_ARTIST.ARTIST_ID.eq(id))
            .fetch()
            .map { unityRepo.convertToShortDto(it) }
            .toList()
    }

    override fun convertToShortDto(record: Record): ArtistShortDto {
        val isFollowed = record.into(USER_FOLLOWS_ARTIST).userProfileUid != null
        return artistConverters.createShortDtoFromRecord(record.into(ARTIST), record.into(COUNTRY), isFollowed)
    }

    override fun convertToFullDto(record: Record): ArtistFullDto {
        val isFollowed = record.into(USER_FOLLOWS_ARTIST).userProfileUid != null
        return artistConverters.createFullDtoFromRecord(record.into(ARTIST), record.into(COUNTRY), isFollowed)
    }

    override fun SelectWhereStep<Record>.whereIdIsInIds(ids: Set<Long>): SelectConditionStep<Record> {
        return this.where(ARTIST.ID.`in`(ids))
    }

    override fun SelectWhereStep<Record>.whereNameIsLike(namePart: String): SelectConditionStep<Record> {
        return this.where(lower(ARTIST.NAME).contains(namePart.lowercase()))
    }

    override fun prepareRecordBeforeSaving(record: ArtistRecord, dto: ArtistWriteDto) {
        artistConverters.transferDataFromDtoToRecord(dto, record)
        record.createdDateTime = dateTimeProvider.getNow()
    }

    override fun postSaveGetId(record: ArtistRecord): Long {
        return record.id ?: throw SaveException("Artist", record.name ?: "NULL")
    }

    override fun preUpdateGetId(dto: ArtistWriteDto): Long {
        return dto.id ?: throw NotFoundException("Artist", dto.id.toString())
    }

    override fun prepareRecordBeforeUpdating(record: ArtistRecord, dto: ArtistWriteDto) {
        artistConverters.transferDataFromDtoToRecord(dto, record)
    }
}