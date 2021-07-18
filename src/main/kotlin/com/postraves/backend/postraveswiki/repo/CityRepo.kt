package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.repo.generic.BaseRepo
import com.postraves.backend.postraveswiki.repo.generic.ByNameRepo
import jooq.tables.records.CityRecord
import jooq.tables.references.CITY
import jooq.tables.references.COUNTRY
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface CityRepo :
    BaseRepo<CityWriteDto, CityDto>,
    ByNameRepo<CityDto>

@Repository
class CityImplRepo(val dsl: DSLContext) :
    CityRepo {

    private fun findByNameWithoutConvertion(name: String): CityRecord {
        val record = dsl
            .selectFrom(CITY)
            .where(CITY.NAME.eq(name))
            .fetchOne()
        return record ?: throw TODO()
    }

    override fun findByName(name: String): CityDto {
        val selectedRecord = dsl
            .selectFrom(CITY.leftOuterJoin(COUNTRY).on(CITY.COUNTRY_NAME.eq(COUNTRY.NAME)))
            .where(CITY.NAME.eq(name))
            .fetchOne()

        val city = selectedRecord?.into(CITY)
        val country = selectedRecord?.into(COUNTRY)

        return if (city != null && country != null) CityDto.createOutOfDbRecords(city, country) else throw TODO()
    }

    override fun save(dto: CityWriteDto): CityDto {
        val recordToSave = dsl.newRecord(CITY)
        dto.transferDataToDbRecord(recordToSave)
        recordToSave.createdDateTime = OffsetDateTime.now()
        recordToSave.store()
        return findByName(recordToSave.name ?: throw TODO())
    }

    override fun update(dto: CityWriteDto) {
        val recordToUpdate = findByNameWithoutConvertion(dto.name)
        dto.transferDataToDbRecord(recordToUpdate)
        recordToUpdate.update()
    }

    override fun deleteByName(name: String) {
        if (dsl.fetchOne(CITY, CITY.NAME.eq(name))
                ?.delete() == null
        ) throw TODO()
    }

    override fun findAll(): List<CityDto> {
        val results = dsl
            .selectFrom(CITY.leftOuterJoin(COUNTRY).on(CITY.COUNTRY_NAME.eq(COUNTRY.NAME)))
            .fetch()
            .map { CityDto.createOutOfDbRecords(it.into(CITY), it.into(COUNTRY)) }
            .toList()
        return results
    }
}