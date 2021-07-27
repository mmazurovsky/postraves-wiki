package com.postraves.backend.postraveswiki.data.dto

import kotlinx.serialization.*

@Serializable
data class CoordinateDto(
    val latitude: Double,
    val longitude: Double,
) : BaseShortDto, BaseFullDto, BaseWriteDto {

//    fun transferDataToDbRecord(countryRecord: CountryRecord) {
//        countryRecord.name = name
//        countryRecord.phoneCode = phoneCode
//        countryRecord.emojiCode = emojiCode
//    }
//
//    companion object FactoryDbRecord {
//        fun createOutOfDbRecords(countryRecord: CountryRecord) : CoordinateDto {
//            return CoordinateDto(
//                name = countryRecord.name ?: throw RecordFieldNullException("Country Name"),
//                phoneCode = countryRecord.phoneCode ?: throw RecordFieldNullException("Country Phone Code"),
//                emojiCode = countryRecord.emojiCode ?: throw RecordFieldNullException("Country Emoji Code"),
//            )
//        }
//    }
    }

