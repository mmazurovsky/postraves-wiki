package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CityRecord
import jooq.tables.records.CountryRecord
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.util.*

interface CityConverters {
    fun createDtoFromRecord(cityRecord: CityRecord, countryRecord: CountryRecord): CityDto
    fun transferDataFromDtoToRecord(dto: CityWriteDto, record: CityRecord)
}

@Service
class CityConvertersImpl(
    private val countryConverters: CountryConverters,
) : CityConverters {

    private fun resolveLocalizedName(cityRecord: CityRecord): String? {
        val userLocale = LocaleContextHolder.getLocale()
        return when {
            userLocale.language.equals(Locale("ru").language) -> {
                cityRecord.cityNameRu
            }
            userLocale.language.equals(Locale("de").language) -> {
                cityRecord.cityNameDe
            }
            userLocale.language.equals(Locale("fr").language) -> {
                cityRecord.cityNameFr
            }
            else -> {
                cityRecord.cityNameEn
            }
        }
    }

    override fun createDtoFromRecord(cityRecord: CityRecord, countryRecord: CountryRecord): CityDto {
        return CityDto(
            name = cityRecord.cityName ?: throw RecordFieldNullException("City Name"),
            localName = resolveLocalizedName(cityRecord) ?: throw RecordFieldNullException("City Local Name"),
            country = countryConverters.createDtoFromRecord(countryRecord)
        )
    }

    override fun transferDataFromDtoToRecord(dto: CityWriteDto, record: CityRecord) {
        record.cityName = dto.name
        record.cityNameRu = dto.nameRu
        record.cityNameEn = dto.nameEn
        record.cityNameDe = dto.nameDe
        record.cityNameFr = dto.nameFr
        record.cityCountryName = dto.countryName
        record.cityTimeOffset = dto.timeOffset
    }
}