package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import jooq.tables.records.CountryRecord
import jooq.tables.references.ARTIST
import jooq.tables.references.COUNTRY
import org.jooq.Record
import org.jooq.conf.Settings
import org.springframework.stereotype.Repository

interface CountryRepo :
    BaseRepo<CountryDto, CountryDto, CountryDto>,
    RepoByName<CountryDto>

@Repository
class CountryRepoImpl(val contextConfig: JooqDSLContextConfig) :
    CountryRepo {

    override fun findByName(name: String): CountryDto {
        val selectedRecord = contextConfig.getDSLContext()
            .selectFrom(COUNTRY)
            .where(COUNTRY.NAME.eq(name))
            .fetchOneInto(COUNTRY)

        return CountryDto.createOutOfDbRecords(selectedRecord)
    }

    override fun save(dto: CountryDto): CountryDto? {
        dto.convertToDbRecord().store()
        return findByName(dto.name)
    }

    override fun update(dto: CountryDto): CountryDto? {
        dto.convertToDbRecord().store()
        return findByName(dto.name)
    }

    override fun deleteByName(name: String): CountryDto {
        val dto = this.findByName(name)
        if (contextConfig.getDSLContext().fetchOne(COUNTRY, COUNTRY.NAME.eq(name))
                ?.delete() == null
        ) throw TODO()
        else return dto
    }

    override fun findAll(): List<CountryDto> {
        val results = contextConfig.getDSLContext()
            .selectFrom(COUNTRY)
            .fetch()
            .map { CountryDto.createOutOfDbRecords(it.into(COUNTRY)) }
            .toList()
        return results
    }
}