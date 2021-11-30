package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.converters.CountryConverters
import com.postraves.backend.postraveswiki.data.dto.reading.CountryDto
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.SaveException
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import jooq.tables.records.CountryRecord
import jooq.tables.references.CITY
import jooq.tables.references.COUNTRY
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface CountryRepo :
    BaseRepo<CountryWriteDto, CountryDto>,
    ByNameRepo<CountryDto>

@Repository
class CountryImplRepo(
    private val countryConverters: CountryConverters,
    private val dateTimeProvider: DateTimeProvider,
) : CountryRepo {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    private fun findByNameWithoutJoins(name: String): CountryRecord {
        val record = dsl.fetchOne(COUNTRY, COUNTRY.COUNTRY_NAME.eq(name))
        return record ?: throw NotFoundException("Country", name)
    }

    override fun findByName(name: String): CountryDto {
        val found = findByNameWithoutJoins(name)
        return countryConverters.createDtoFromRecord(found.into(COUNTRY))
    }

    override fun save(dto: CountryWriteDto): CountryDto {
        val countryToSave = dsl.newRecord(COUNTRY)
        countryConverters.transferDataFromDtoToRecord(dto, countryToSave)
        countryToSave.countryCreatedDateTime = dateTimeProvider.getNow()
        countryToSave.countryUpdatedDateTime = dateTimeProvider.getNow()
        countryToSave.store()
        return findByName(countryToSave.countryName ?: throw SaveException("Country", dto.name))
    }

    override fun update(dto: CountryWriteDto) {
        val countryToUpdate = findByNameWithoutJoins(dto.name)
        countryConverters.transferDataFromDtoToRecord(dto, countryToUpdate)
        countryToUpdate.countryUpdatedDateTime = dateTimeProvider.getNow()
        countryToUpdate.update()
    }

    override fun deleteByName(name: String) {
        findByNameWithoutJoins(name).delete()
    }

    override fun findAll(): List<CountryDto> {
        val results = dsl
            .selectFrom(COUNTRY)
            .orderBy(COUNTRY.COUNTRY_UPDATED_DATE_TIME.asc())
            .fetch()
            .map {
                countryConverters.createDtoFromRecord(it.into(COUNTRY))
            }
            .toList()
        return results
    }

    override fun findByPartOfName(namePart: String): List<CountryDto> {
        val results = dsl
            .selectFrom(COUNTRY)
            .where(DSL.lower(COUNTRY.COUNTRY_NAME).contains(namePart.lowercase()))
            .fetch()
            .map {
                countryConverters.createDtoFromRecord(it.into(COUNTRY))
            }
            .toList()
        return results
    }
}