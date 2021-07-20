package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.repo.generic.BaseRepo
import com.postraves.backend.postraveswiki.repo.generic.ByIdRepo
import com.postraves.backend.postraveswiki.repo.generic.RatingRepo
import jooq.tables.Artist
import jooq.tables.records.ArtistRecord
import jooq.tables.references.ARTIST
import jooq.tables.references.COUNTRY
import jooq.tables.references.USER_FOLLOWS_ARTIST
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectWhereStep
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface ArtistRepo :
    BaseRepo<ArtistWriteDto, ArtistShortDto>,
    ByIdRepo<ArtistFullDto, ArtistShortDto>,
    RatingRepo<ArtistShortDto>

@Repository
class ArtistRepoImpl(private val dslContextConfig: JooqDSLContextConfig) : ArtistRepo {

    private val dsl: DSLContext by lazy { dslContextConfig.getDSLContext() }

    private fun findByIdWithoutJoins(id: Long): ArtistRecord {
        val record =
//            dsl
//            .selectFrom(ARTIST)
//            .where(ARTIST.ID.eq(id))
//            .fetchOne()
            dsl.fetchOne(ARTIST, ARTIST.ID.eq(id))
        return record ?: throw TODO()
    }

    private fun findByIdWithJoins(id: Long): Record {
        val record = dsl
            .selectFrom(ARTIST.leftOuterJoin(COUNTRY).on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME)))
            .where(ARTIST.ID.eq(id))
            .fetchOne()
        return record ?: throw TODO()
    }

    private fun selectArtistList(): SelectWhereStep<Record> {
        val select = dsl
            .selectFrom(
                ARTIST
                    .leftOuterJoin(COUNTRY)
                    .on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME))
            )
        return select
    }

    override fun findById(id: Long): ArtistFullDto {
        val selectedRecord = findByIdWithJoins(id)

        return ArtistFullDto.createOutOfDbRecords(selectedRecord.into(ARTIST), selectedRecord.into(COUNTRY), false)
    }

    override fun findByIdForUser(authUid: String, id: Long): ArtistFullDto {
        val selectedRecord = dsl
            .selectFrom(
                ARTIST
                    .leftOuterJoin(COUNTRY)
                    .on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME))
                    .leftOuterJoin(USER_FOLLOWS_ARTIST)
                    .on(ARTIST.ID.eq(USER_FOLLOWS_ARTIST.ARTIST_ID), USER_FOLLOWS_ARTIST.USER_PROFILE_UID.eq(authUid))
            )
            .where(ARTIST.ID.eq(id))
            .fetchOne() ?: throw TODO()

        val isFollowed = selectedRecord.into(USER_FOLLOWS_ARTIST).userProfileUid != null

        return ArtistFullDto.createOutOfDbRecords(selectedRecord.into(ARTIST), selectedRecord.into(COUNTRY), isFollowed)
    }

    override fun save(dto: ArtistWriteDto): ArtistShortDto {
        val artistToSave = dsl.newRecord(ARTIST)
        dto.transferDataToDbRecord(artistToSave)
        // TODO separate function for initialization
        artistToSave.createdDateTime = OffsetDateTime.now()
        artistToSave.overallFollowersCount = 0
        artistToSave.baseRating = dto.soundcloudFollowersCount?.div(5) ?: 0
        artistToSave.store()
        val id = artistToSave.id ?: throw TODO()
        val record = findByIdWithJoins(id)
        return ArtistShortDto.createOutOfDbRecords(record.into(ARTIST), record.into(COUNTRY))
    }

    override fun update(dto: ArtistWriteDto) {
        dto.id ?: throw TODO()
        val artistToUpdate = findByIdWithoutJoins(dto.id)
        dto.transferDataToDbRecord(artistToUpdate)
        artistToUpdate.update()
        artistToUpdate.id ?: throw TODO()
    }

    override fun deleteById(id: Long) {
        findByIdWithoutJoins(id).delete()
    }

    override fun findListByIds(ids: Set<Long>): List<ArtistShortDto> {
        val results = selectArtistList()
            .where(ARTIST.ID.`in`(ids))
            .fetch()
            .map { ArtistShortDto.createOutOfDbRecords(it.into(ARTIST), it.into(COUNTRY)) }
            .toList()
        return results
    }

    override fun findOverallTopInCountry(countryName: String, maxQuantity: Int): List<ArtistShortDto> {
        val results = selectArtistList()
            .where(ARTIST.COUNTRY_NAME.eq(countryName))
            .orderBy((ARTIST.BASE_RATING + ARTIST.OVERALL_FOLLOWERS_COUNT).desc())
            .limit(maxQuantity)
            .offset(0)
            .fetch()
            .map { ArtistShortDto.createOutOfDbRecords(it.into(ARTIST), it.into(COUNTRY)) }
            .toList()
        return results
    }

    override fun findOverallTopInCountryForUser(
        authUid: String,
        countryName: String,
        maxQuantity: Int
    ): List<ArtistShortDto> {
        TODO("Not yet implemented")
    }

    override fun changeBaseRating(id: Long, newBaseRating: Int) {
        val artistRecord = findByIdWithoutJoins(id)
        artistRecord.baseRating = newBaseRating
        artistRecord.update()
    }

    override fun incrementOverallFollowers(id: Long) {
        val artistRecord = findByIdWithoutJoins(id)
        artistRecord.overallFollowersCount = artistRecord.overallFollowersCount?.plus(1) ?: throw TODO()
        artistRecord.update()
    }

    override fun decrementOverallFollowers(id: Long) {
        val artistRecord = findByIdWithoutJoins(id)
        artistRecord.overallFollowersCount = artistRecord.overallFollowersCount?.minus(1) ?: throw TODO()
        artistRecord.update()
    }

    override fun findAll(): List<ArtistShortDto> {
        val results = selectArtistList()
            .fetch()
            .map { ArtistShortDto.createOutOfDbRecords(it.into(ARTIST), it.into(COUNTRY)) }
            .toList()
        return results
    }
}