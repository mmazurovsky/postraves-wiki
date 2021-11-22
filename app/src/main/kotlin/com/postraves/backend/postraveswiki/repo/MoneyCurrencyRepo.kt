package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.converters.MoneyCurrencyConverters
import com.postraves.backend.postraveswiki.data.dto.MoneyCurrencyDto
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.SaveException
import jooq.tables.records.MoneyCurrencyRecord
import jooq.tables.references.CITY
import jooq.tables.references.MONEY_CURRENCY
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

interface MoneyCurrencyRepo :
    BaseRepo<MoneyCurrencyDto, MoneyCurrencyDto>,
    ByNameRepo<MoneyCurrencyDto>

@Repository
class MoneyCurrencyRepoImpl(
    private val moneyCurrencyConverters: MoneyCurrencyConverters,
    ) : MoneyCurrencyRepo {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext
    private val thisTable = MONEY_CURRENCY


    private fun findByNameWithoutJoins(name: String): MoneyCurrencyRecord {
        val record = dsl.fetchOne(thisTable, thisTable.MONEY_CURRENCY_NAME.eq(name))
        return record ?: throw NotFoundException("Money Currency", name)
    }

    override fun findByName(name: String): MoneyCurrencyDto {
        val found = findByNameWithoutJoins(name)
        return moneyCurrencyConverters.createDtoFromRecord(found.into(thisTable))
    }

    override fun save(dto: MoneyCurrencyDto): MoneyCurrencyDto {
        val moneyCurrencyToSave = dsl.newRecord(thisTable)
        moneyCurrencyConverters.transferDataFromDtoToRecord(dto, moneyCurrencyToSave)
        moneyCurrencyToSave.store()
        return findByName(moneyCurrencyToSave.moneyCurrencyName ?: throw SaveException("Money Currency", dto.name))
    }

    override fun update(dto: MoneyCurrencyDto) {
        val countryToUpdate = findByNameWithoutJoins(dto.name)
        moneyCurrencyConverters.transferDataFromDtoToRecord(dto, countryToUpdate)
        countryToUpdate.update()
    }

    override fun deleteByName(name: String) {
        findByNameWithoutJoins(name).delete()
    }

    override fun findAll(): List<MoneyCurrencyDto> {
        val results = dsl
            .selectFrom(thisTable)
            .fetch()
            .map {
                moneyCurrencyConverters.createDtoFromRecord(it.into(thisTable))
            }
            .toList()
        return results
    }

    override fun findByPartOfName(namePart: String): List<MoneyCurrencyDto> {
        val results = dsl
            .selectFrom(thisTable)
            .where(DSL.lower(CITY.CITY_NAME).contains(namePart.lowercase()))
            .fetch()
            .map {
                moneyCurrencyConverters.createDtoFromRecord(it.into(thisTable))
            }
            .toList()
        return results
    }
}