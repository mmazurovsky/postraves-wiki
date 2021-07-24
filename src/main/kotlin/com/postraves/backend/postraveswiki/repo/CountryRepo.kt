package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.repo.generic.BaseRepo
import com.postraves.backend.postraveswiki.repo.generic.ByNameRepo
import com.postraves.backend.postraveswiki.repo.generic.FindByName
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import jooq.tables.references.CITY
import jooq.tables.references.COUNTRY
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectWhereStep
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface CountryRepo :
    BaseRepo<CountryDto, CountryDto>,
    ByNameRepo<CountryDto>,
    FindByName<CountryDto>

@Repository
class CountryImplRepo :
    CountryRepo {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    private fun findByNameWithoutJoins(name: String): CountryRecord {
        val record = dsl.fetchOne(COUNTRY, COUNTRY.NAME.eq(name))
        return record ?: throw TODO()
    }

    override fun findByName(name: String): CountryDto {
        val found = findByNameWithoutJoins(name)
        return CountryDto.createOutOfDbRecords(found.into(COUNTRY))
    }

    override fun save(dto: CountryDto): CountryDto {
        val countryToSave = dsl.newRecord(COUNTRY)
        dto.transferDataToDbRecord(countryToSave)
        countryToSave.createdDateTime = OffsetDateTime.now()
        countryToSave.store()
        return findByName(countryToSave.name ?: throw TODO())
    }

    override fun update(dto: CountryDto) {
        val countryToUpdate = findByNameWithoutJoins(dto.name)
        dto.transferDataToDbRecord(countryToUpdate)
        countryToUpdate.update()
    }

    override fun deleteByName(name: String) {
        findByNameWithoutJoins(name).delete()
    }

    override fun findAll(): List<CountryDto> {
        val results = dsl
            .selectFrom(COUNTRY)
            .fetch()
            .map { CountryDto.createOutOfDbRecords(it.into(COUNTRY)) }
            .toList()
        return results
    }

    override fun findByPartOfName(namePart: String): List<CountryDto> {
        val results = dsl
            .selectFrom(COUNTRY)
            .where(DSL.lower(CITY.NAME).contains(namePart.lowercase()))
            .fetch()
            .map { CountryDto.createOutOfDbRecords(it.into(COUNTRY)) }
            .toList()
        return results
    }
}