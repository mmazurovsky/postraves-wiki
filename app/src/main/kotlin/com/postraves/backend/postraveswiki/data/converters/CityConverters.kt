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
        return if (userLocale.language.equals(Locale("ru").language)) {
            cityRecord.nameRu
        } else if (userLocale.language.equals(Locale("de").language)) {
            cityRecord.nameDe
        } else if (userLocale.language.equals(Locale("fr").language)) {
            cityRecord.nameFr
        } else {
            cityRecord.nameUk
        }
    }

    override fun createDtoFromRecord(cityRecord: CityRecord, countryRecord: CountryRecord): CityDto {
        return CityDto(
            name = cityRecord.name ?: throw RecordFieldNullException("City Name"),
            localizedName = resolveLocalizedName(cityRecord) ?: throw RecordFieldNullException("City Name"),
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