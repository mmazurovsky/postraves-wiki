package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import jooq.tables.records.ArtistRecord
import jooq.tables.references.ARTIST
import jooq.tables.references.COUNTRY
import jooq.tables.references.USER_FOLLOWS_ARTIST
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectWhereStep
import org.jooq.impl.DSL.lower
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface ArtistRepo :
    BaseRepo<ArtistWriteDto, ArtistShortDto>,
    ByIdRepo<ArtistFullDto, ArtistShortDto>,
    FindByNameRepo<ArtistShortDto>

@Repository
class ArtistRepoImpl(private val dslContextConfig: JooqDSLContextConfig) : ArtistRepo {

    @Autowired @Lazy
    private lateinit var dsl: DSLContext

    private fun findByIdWithoutJoins(id: Long): ArtistRecord {
        val record = dsl.fetchOne(ARTIST, ARTIST.ID.eq(id))
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
        artistToSave.createdDateTime = OffsetDateTime.now()
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

    override fun findAll(): List<ArtistShortDto> {
        val results = selectArtistList()
            .fetch()
            .map { ArtistShortDto.createOutOfDbRecords(it.into(ARTIST), it.into(COUNTRY)) }
            .toList()
        return results
    }

    override fun findByPartOfName(namePart: String): List<ArtistShortDto> {
        val results = selectArtistList()
            .where(lower(ARTIST.NAME).contains(namePart.lowercase()))
            .fetch()
            .map { ArtistShortDto.createOutOfDbRecords(it.into(ARTIST), it.into(COUNTRY)) }
            .toList()
        return results
    }
}