package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import jooq.tables.records.ArtistRecord
import jooq.tables.references.ARTIST
import jooq.tables.references.COUNTRY
import org.jooq.Record
import org.springframework.stereotype.Repository

interface ArtistRepo : BaseOperationsRepo<ArtistRecord, ArtistWriteDto, ArtistShortDto, ArtistFullDto>

@Repository
class ArtistRepoImpl(val contextConfig: JooqDSLContextConfig, val saveRepo: SaveRepo<ArtistRecord, ArtistWriteDto>) :
    ArtistRepo {

    override fun findById(id: Long): ArtistFullDto {
        val selectedRecord: Record? = contextConfig.getDSLContext()
            .selectFrom(ARTIST.fullOuterJoin(COUNTRY).on(ARTIST.COUNTRY_NAME.eq(COUNTRY.NAME)))
            .where(ARTIST.ID.eq(id))
            .fetchOne()

        val artistRecord = selectedRecord?.into(ARTIST)
        val countryRecord = selectedRecord?.into(COUNTRY)

        return ArtistFullDto.createOutOfDbRecords(artistRecord, countryRecord)
    }

    override fun save(dto: ArtistWriteDto): ArtistFullDto? {
        val recordId = saveRepo.save(dto)
        return this.findById(recordId)
    }

    override fun update(dto: ArtistWriteDto): ArtistFullDto? {
        val recordId = saveRepo.save(dto)
        return this.findById(recordId)
    }

    override fun deleteById(id: Long): ArtistFullDto {
        //TODO two selects instead of one
        val dto = this.findById(id)
        if (contextConfig.getDSLContext().fetchOne(ARTIST, ARTIST.ID.eq(id))
                ?.delete() == null
        ) throw TODO()
        else return dto
    }
}