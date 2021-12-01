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
import jooq.tables.Artist
import jooq.tables.records.ArtistRecord
import jooq.tables.references.*
import org.jooq.*
import org.jooq.impl.DSL.lower
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface ArtistRepo :
    BaseRepo<ArtistWriteDto, ArtistShortDto>,
    ByIdRepo<ArtistFullDto, ArtistShortDto>,
    FollowableRepo<ArtistShortDto> {
    fun getUnitiesOfArtist(userId: Long?, id: Long): List<UnityShortDto>
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

    @Qualifier("getDSLContext")
    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    override fun SelectJoinStep<Record>.joinLocation(): SelectOnConditionStep<Record> {
        return joinArtistLocation()
    }

    override fun SelectJoinStep<Record>.joinUserFollow(userId: Long): SelectOnConditionStep<Record> {
        return joinArtistUserFollow(userId)
    }

    override fun SelectJoinStep<Record>.joinOtherData(): SelectOnConditionStep<Record>? {
        return null
    }

    override fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record> {
        return this.where(ARTIST.ARTIST_ID.eq(id))
    }

    private fun SelectJoinStep<Record>.joinUserFollowUnity(userId: Long): SelectOnConditionStep<Record> {
        return this.leftOuterJoin(USER_FOLLOWS_UNITY)
            .on(
                UNITY.UNITY_ID.eq(USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_UNITY_ID),
                USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_USER_PROFILE_ID.eq(userId)
            )
    }

    override fun getUnitiesOfArtist(userId: Long?, id: Long): List<UnityShortDto> {
        return dsl
            .select()
            .from(UNITY_ARTIST)
            .leftOuterJoin(UNITY).on(UNITY.UNITY_ID.eq(UNITY_ARTIST.UNITY_ARTIST_UNITY_ID))
            .leftOuterJoin(COUNTRY).on(COUNTRY.COUNTRY_NAME.eq(UNITY.UNITY_COUNTRY_NAME))
            .apply { if (userId != null) joinUserFollowUnity(userId) }
            .where(UNITY_ARTIST.UNITY_ARTIST_ARTIST_ID.eq(id))
            .fetch()
            .map { unityRepo.convertToShortDto(it) }
            .toList()
    }

    override fun convertToShortDto(record: Record): ArtistShortDto {
        val isFollowed = record.into(USER_FOLLOWS_ARTIST).userFollowsArtistUserProfileId != null
        return artistConverters.createShortDtoFromRecord(record.into(ARTIST), record.into(COUNTRY), isFollowed)
    }

    override fun convertToFullDto(record: Record): ArtistFullDto {
        val isFollowed = record.into(USER_FOLLOWS_ARTIST).userFollowsArtistUserProfileId != null
        return artistConverters.createFullDtoFromRecord(record.into(ARTIST), record.into(COUNTRY), isFollowed)
    }

    override fun SelectWhereStep<Record>.whereIdIsInIds(ids: Set<Long>): SelectConditionStep<Record> {
        return this.where(ARTIST.ARTIST_ID.`in`(ids))
    }

    override fun SelectWhereStep<Record>.whereNameIsLike(namePart: String): SelectConditionStep<Record> {
        return this.where(lower(ARTIST.ARTIST_NAME).contains(namePart.lowercase()))
    }

    override fun prepareRecordBeforeSaving(record: ArtistRecord, dto: ArtistWriteDto) {
        artistConverters.transferDataFromDtoToRecord(dto, record)
        record.artistCreatedDateTime = dateTimeProvider.getNow()
        record.artistUpdatedDateTime = dateTimeProvider.getNow()
    }

    override fun postSaveGetId(record: ArtistRecord): Long {
        return record.artistId ?: throw SaveException("Artist", record.artistName ?: "NULL")
    }

    override fun preUpdateCheckId(dto: ArtistWriteDto): Long {
        return dto.id ?: throw NotFoundException("Artist", dto.id.toString())
    }

    override fun prepareRecordBeforeUpdating(record: ArtistRecord, dto: ArtistWriteDto) {
        artistConverters.transferDataFromDtoToRecord(dto, record)
        record.artistUpdatedDateTime = dateTimeProvider.getNow()
    }

    override fun updateUpdatedDateTimeInRecord(recordToUpdate: ArtistRecord) {
        recordToUpdate.artistUpdatedDateTime = dateTimeProvider.getNow()
    }

//    override fun sortByUpdatedDateTime(list: SelectOnConditionStep<Record>): List<Record> {
//        return list.sortedWith(compareBy { it.into(ARTIST).artistUpdatedDateTime })
//    }

    override fun getUpdatedDateTimeOrderField(): SortField<OffsetDateTime?> {
        return ARTIST.ARTIST_UPDATED_DATE_TIME.desc()
    }
}