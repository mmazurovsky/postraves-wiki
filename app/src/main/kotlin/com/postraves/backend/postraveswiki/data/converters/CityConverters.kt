package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import org.springframework.stereotype.Service

interface CityConverters {
    fun createDtoFromRecord(cityRecord: CityRecord, countryRecord: CountryRecord): CityDto
    fun transferDataFromDtoToRecord(dto: CityWriteDto, record: CityRecord)
}

@Service
class CityConvertersImpl(
    private val countryConverters: CountryConverters,
) : CityConverters {

    override fun createDtoFromRecord(cityRecord: CityRecord, countryRecord: CountryRecord): CityDto {
        return CityDto(
            name = cityRecord.name ?: throw RecordFieldNullException("City Name"),
            nameRu = cityRecord.nameRu ?: throw RecordFieldNullException("City Name Ru"),
            nameUk = cityRecord.nameUk ?: throw RecordFieldNullException("City Name Uk"),
            nameDe = cityRecord.nameDe ?: throw RecordFieldNullException("City Name De"),
            nameFr = cityRecord.nameFr ?: throw RecordFieldNullException("City Name Fr"),
            country = countryConverters.createDtoFromRecord(countryRecord)
        )
    }

    override fun transferDataFromDtoToRecord(dto: CityWriteDto, record: CityRecord) {
        record.name = dto.name
        record.nameRu = dto.nameRu
        record.nameUk = dto.nameUk
        record.nameDe = dto.nameDe
        record.nameFr = dto.nameFr
        record.countryName = dto.countryName
        record.timeOffset = dto.timeOffset
    }
}