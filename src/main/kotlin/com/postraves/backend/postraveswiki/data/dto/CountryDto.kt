package com.postraves.backend.postraveswiki.data.dto

import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CountryRecord
import kotlinx.serialization.*

@Serializable
data class CountryDto(
    val name: String,
    val phoneCode: String,
    val emojiCode: String,
) : BaseShortDto, BaseFullDto, BaseWriteDto {

    fun transferDataToDbRecord(countryRecord: CountryRecord) {
        countryRecord.name = name
        countryRecord.phoneCode = phoneCode
        countryRecord.emojiCode = emojiCode
    }
    
    companion object FactoryDbRecord {
        fun createOutOfDbRecords(countryRecord: CountryRecord) : CountryDto {
            return CountryDto(
                name = countryRecord.name ?: throw RecordFieldNullException("Country Name"),
                phoneCode = countryRecord.phoneCode ?: throw RecordFieldNullException("Country Phone Code"),
                emojiCode = countryRecord.emojiCode ?: throw RecordFieldNullException("Country Emoji Code"),
            )
        }
    }
    }

