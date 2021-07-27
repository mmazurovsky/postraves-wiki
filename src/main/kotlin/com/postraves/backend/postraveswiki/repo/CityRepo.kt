package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.SaveException
import jooq.tables.records.CityRecord
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

interface CityRepo :
    BaseRepo<CityWriteDto, CityDto>,
    ByNameRepo<CityDto>

@Repository
class CityImplRepo :
    CityRepo {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    private fun findByNameWithoutJoins(name: String): CityRecord {
        val record = dsl.fetchOne(CITY, CITY.NAME.eq(name))
        return record ?: throw NotFoundException("City", name)
    }

    private fun findByNameWithJoins(name: String): Record {
        val selectedRecord = dsl
            .selectFrom(CITY.leftOuterJoin(COUNTRY).on(CITY.COUNTRY_NAME.eq(COUNTRY.NAME)))
            .where(CITY.NAME.eq(name))
            .fetchOne()
        return selectedRecord ?: throw NotFoundException("City", name)
    }

    private fun selectCityList(): SelectWhereStep<Record> {
        val select = dsl
            .selectFrom(
                CITY
                    .leftOuterJoin(COUNTRY)
                    .on(CITY.COUNTRY_NAME.eq(COUNTRY.NAME))
            )
        return select
    }

    override fun findByName(name: String): CityDto {
        val selectedRecord = findByNameWithJoins(name)
        return CityDto.createOutOfDbRecords(selectedRecord.into(CITY), selectedRecord.into(COUNTRY))
    }

    override fun save(dto: CityWriteDto): CityDto {
        val recordToSave = dsl.newRecord(CITY)
        dto.transferDataToDbRecord(recordToSave)
        recordToSave.createdDateTime = OffsetDateTime.now()
        recordToSave.store()
        return findByName(recordToSave.name ?: throw SaveException("City", dto.name))
    }

    override fun update(dto: CityWriteDto) {
        val recordToUpdate = findByNameWithoutJoins(dto.name)
        dto.transferDataToDbRecord(recordToUpdate)
        recordToUpdate.update()
    }

    override fun deleteByName(name: String) {
        findByNameWithoutJoins(name).delete()
    }

    override fun findAll(): List<CityDto> {
        val results = dsl
            .selectFrom(CITY.leftOuterJoin(COUNTRY).on(CITY.COUNTRY_NAME.eq(COUNTRY.NAME)))
            .fetch()
            .map { CityDto.createOutOfDbRecords(it.into(CITY), it.into(COUNTRY)) }
            .toList()
        return results
    }

    override fun findByPartOfName(namePart: String): List<CityDto> {
        val results = selectCityList()
            .where(DSL.lower(CITY.NAME).contains(namePart.lowercase()))
            .fetch()
            .map { CityDto.createOutOfDbRecords(it.into(CITY), it.into(COUNTRY)) }
            .toList()
        return results
    }
}