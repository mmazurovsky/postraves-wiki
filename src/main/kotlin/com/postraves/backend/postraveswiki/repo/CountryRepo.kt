package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import jooq.tables.records.CountryRecord
import jooq.tables.references.COUNTRY
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface CountryRepo :
    BaseRepo<CountryDto, CountryDto, CountryDto>,
    ByNameRepo<CountryDto>

@Repository
class CountryImplRepo(val dsl: DSLContext) :
    CountryRepo {

    private fun findByNameWithoutConvertion(name: String) : CountryRecord {
        val record = dsl
            .selectFrom(COUNTRY)
            .where(COUNTRY.NAME.eq(name))
            .fetchOne()
        return record ?: throw TODO()
    }

    override fun findByName(name: String): CountryDto {
        val selectedRecord = dsl
            .selectFrom(COUNTRY)
            .where(COUNTRY.NAME.eq(name))
            .fetchOneInto(COUNTRY)
        return if (selectedRecord != null) CountryDto.createOutOfDbRecords(selectedRecord) else throw TODO()
    }

    override fun save(dto: CountryDto): CountryDto? {
        val countryToSave = dsl.newRecord(COUNTRY)
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
        if (dsl.fetchOne(COUNTRY, COUNTRY.NAME.eq(name))
                ?.delete() == null
        ) throw TODO()
        else return dto
    }

    override fun findAll(): List<CountryDto> {
        val results = dsl
            .selectFrom(COUNTRY)
            .fetch()
            .map { CountryDto.createOutOfDbRecords(it.into(COUNTRY)) }
            .toList()
        return results
    }
}