package com.postraves.backend.postraveswiki.data.dto

import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CountryRecord
import kotlinx.serialization.*

@Serializable
data class CountryDto(
    val name: String,
    val phoneCode: String,
    var emojiCode: String?,
) : BaseShortDto, BaseFullDto, BaseWriteDto {

    init {
        if (emojiCode == null)
            emojiCode = createEmojiCode(name)
    }

    private fun createEmojiCode(countryCode: String): String {
        val flagOffset = 0x1F1E6
        val asciiOffset = 0x41
        val firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset
        val secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset
        return (String(Character.toChars(firstChar))
                + String(Character.toChars(secondChar)))
    }

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

