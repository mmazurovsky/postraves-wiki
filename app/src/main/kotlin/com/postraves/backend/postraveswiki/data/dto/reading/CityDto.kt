package com.postraves.backend.postraveswiki.data.dto.reading

import com.postraves.backend.postraveswiki.data.dto.BaseFullDto
import com.postraves.backend.postraveswiki.data.dto.BaseShortDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import kotlinx.serialization.*

@Serializable
data class CityDto(
    val name: String,
    val country: CountryDto,
) : BaseShortDto, BaseFullDto {
    
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(cityRecord: CityRecord, countryRecord: CountryRecord) : CityDto {
            return CityDto(
                name = cityRecord.name ?: throw RecordFieldNullException("City Name"),
                country = CountryDto.createOutOfDbRecords(countryRecord)
            )
        }
    }
    }

