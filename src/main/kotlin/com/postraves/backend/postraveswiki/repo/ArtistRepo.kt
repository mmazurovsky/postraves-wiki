package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import jooq.tables.records.ArtistRecord
import jooq.tables.references.ARTIST
import jooq.tables.references.COUNTRY
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectWhereStep
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface ArtistRepo :
    BaseRepo<ArtistWriteDto, ArtistShortDto, ArtistFullDto>,
    ByIdRepo<ArtistFullDto>,
    RatingRepo<ArtistShortDto>

@Repository
class ArtistRepoImpl(private val dslContextConfig: JooqDSLContextConfig) : ArtistRepo {

    private val dsl: DSLContext by lazy { dslContextConfig.getDSLContext() }

    private fun findByIdWithoutConvertion(id: Long): ArtistRecord {
        val record = dsl
            .selectFrom(ARTIST)
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
        val selectedRecord: Record? = dsl
            .selectFrom(
                ARTIST
                    .leftOuterJoin(COUNTRY)
                    .on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME))
            )
            .where(ARTIST.ID.eq(id))
            .fetchOne()

        val artistRecord = selectedRecord?.into(ARTIST)
        val countryRecord = selectedRecord?.into(COUNTRY)

        return ArtistFullDto.createOutOfDbRecords(artistRecord, countryRecord)
    }

    override fun save(dto: ArtistWriteDto): ArtistFullDto? {
        val artistToSave = dsl.newRecord(ARTIST)
        dto.transferDataToDbRecord(artistToSave)
        artistToSave.createdDateTime = OffsetDateTime.now()
        artistToSave.overallFollowersCount = 0
        artistToSave.baseRating = dto.soundcloudFollowersCount?.div(5) ?: 0
        artistToSave.store()
        val id = artistToSave.id ?: throw TODO()
        return this.findById(id)
    }

    override fun update(dto: ArtistWriteDto): ArtistFullDto? {
        dto.id ?: throw TODO()
        val artistToUpdate = findByIdWithoutConvertion(dto.id)
        dto.transferDataToDbRecord(artistToUpdate)
        artistToUpdate.update()
        val id = artistToUpdate.id ?: throw TODO()
        return this.findById(id)
    }

    override fun deleteById(id: Long): ArtistFullDto {
        val dto = this.findById(id)
        if (dsl.fetchOne(ARTIST, ARTIST.ID.eq(id))
                ?.delete() == null
        ) throw TODO()
        else return dto
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

    override fun findWeeklyTopInCountry(countryName: String, maxQuantity: Int): List<ArtistShortDto> {
        TODO("Not yet implemented")
    }

    override fun findOfTheWeekInCountry(countryName: String): ArtistShortDto {
        TODO("Not yet implemented")
    }

    override fun changeBaseRating(id: Long, newBaseRating: Int) {
        val artistRecord = dsl
            .selectFrom(ARTIST)
            .where(ARTIST.ID.eq(id))
            .fetchOne()

        artistRecord?.baseRating = newBaseRating
        artistRecord?.store()
    }

    override fun findAll(): List<ArtistShortDto> {
        val results = selectArtistList()
            .fetch()
            .map { ArtistShortDto.createOutOfDbRecords(it.into(ARTIST), it.into(COUNTRY)) }
            .toList()
        return results
    }
}