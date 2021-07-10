package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.config.JooqDSLContextConfig
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import jooq.tables.records.CountryRecord
import jooq.tables.references.COUNTRY
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface CountryRepo :
    BaseRepo<CountryDto, CountryDto, CountryDto>,
    ByNameRepo<CountryDto>

@Repository
class CountryImplRepo(val contextConfig: JooqDSLContextConfig) :
    CountryRepo {

    private fun findByNameWithoutConvertion(name: String) : CountryRecord {
        val record = contextConfig.getDSLContext()
            .selectFrom(COUNTRY)
            .where(COUNTRY.NAME.eq(name))
            .fetchOne()
        return record ?: throw TODO()
    }

    override fun findByName(name: String): CountryDto {
        val selectedRecord = contextConfig.getDSLContext()
            .selectFrom(COUNTRY)
            .where(COUNTRY.NAME.eq(name))
            .fetchOneInto(COUNTRY)
        return CountryDto.createOutOfDbRecords(selectedRecord)
    }

    override fun save(dto: CountryDto): CountryDto? {
        val countryToSave = contextConfig.getDSLContext().newRecord(COUNTRY)
        dto.transferDataToDbRecord(countryToSave)
        countryToSave.createdDateTime = OffsetDateTime.now()
        countryToSave.store()
        return findByName(dto.name)
    }

    override fun update(dto: CountryDto): CountryDto? {
        val countryToUpdate = findByNameWithoutConvertion(dto.name)
        dto.transferDataToDbRecord(countryToUpdate)
        countryToUpdate.update()
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