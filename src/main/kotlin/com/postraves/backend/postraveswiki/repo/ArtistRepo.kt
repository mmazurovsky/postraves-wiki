package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.data.dto.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.ArtistShortDto
import jooq.tables.references.ARTIST
import jooq.tables.references.COUNTRY
import org.jooq.Record
import org.springframework.stereotype.Repository

interface ArtistRepo : BaseOperationsRepo<ArtistShortDto, ArtistFullDto>

@Repository
class ArtistRepoImpl(val contextConfig : JooqDSLContextConfig) : ArtistRepo {

    override fun findById(id: Long): ArtistFullDto {
        val selectedRecord: Record? = contextConfig.getContext()
            .selectFrom(ARTIST.fullOuterJoin(COUNTRY).on(ARTIST.COUNTRY_ID.eq(COUNTRY.ID)))
            .where(ARTIST.ID.eq(id))
            .fetchOne()

        val artistRecord = selectedRecord?.into(ARTIST)
        val countryRecord = selectedRecord?.into(COUNTRY)

        return ArtistFullDto.createOutOfDbRecords(artistRecord, countryRecord)
    }

    override fun save(dto: ArtistFullDto): ArtistFullDto? {
        TODO("Not yet implemented")
    }

    override fun update(dto: ArtistFullDto): ArtistFullDto? {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long): ArtistFullDto {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<ArtistShortDto> {
        TODO("Not yet implemented")
    }
}