package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.MoneyCurrencyDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.ArtistRecord
import jooq.tables.records.CountryRecord
import jooq.tables.records.MoneyCurrencyRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import org.springframework.stereotype.Service

interface MoneyCurrencyConverters {
    fun createDtoFromRecord(
        moneyCurrencyRecord: MoneyCurrencyRecord,
    ): MoneyCurrencyDto

    fun transferDataFromDtoToRecord(dto: MoneyCurrencyDto, record: MoneyCurrencyRecord)
}

@Service
class MoneyCurrencyConvertersImpl(
) : MoneyCurrencyConverters {

    override fun createDtoFromRecord(
        moneyCurrencyRecord: MoneyCurrencyRecord,
    ): MoneyCurrencyDto {
        return MoneyCurrencyDto(
            name = moneyCurrencyRecord.moneyCurrencyName ?: throw RecordFieldNullException("Money Currency Name"),
            symbol = moneyCurrencyRecord.moneyCurrencySymbol ?: throw RecordFieldNullException("Money Currency Symbol"),
        )
    }

    override fun transferDataFromDtoToRecord(dto: MoneyCurrencyDto, record: MoneyCurrencyRecord) {
        record.moneyCurrencyName = dto.name
        record.moneyCurrencySymbol = dto.symbol
    }
}
