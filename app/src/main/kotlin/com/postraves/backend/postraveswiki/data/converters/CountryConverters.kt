package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CountryRecord
import org.springframework.stereotype.Service

interface CountryConverters {
    fun createDtoFromRecord(countryRecord: CountryRecord): CountryDto
    fun transferDataFromDtoToRecord(dto: CountryDto, record: CountryRecord)
}

@Service
class CountryConvertersImpl : CountryConverters {

    override fun createDtoFromRecord(countryRecord: CountryRecord): CountryDto {
        return CountryDto(
            name = countryRecord.name ?: throw RecordFieldNullException("Country Name"),
            nameRu = countryRecord.nameRu ?: throw RecordFieldNullException("Country Name Ru"),
            nameUk = countryRecord.nameUk ?: throw RecordFieldNullException("Country Name Uk"),
            nameDe = countryRecord.nameDe ?: throw RecordFieldNullException("Country Name De"),
            nameFr = countryRecord.nameFr ?: throw RecordFieldNullException("Country Name Fr"),
            phoneCode = countryRecord.phoneCode ?: throw RecordFieldNullException("Country phone code"),
            emojiCode = countryRecord.emojiCode
        )
    }

    override fun transferDataFromDtoToRecord(dto: CountryDto, record: CountryRecord) {
        val emojiCode = createEmojiCode(dto.name)
        record.name = dto.name
        record.nameRu = dto.nameRu
        record.nameUk = dto.nameUk
        record.nameDe = dto.nameDe
        record.nameFr = dto.nameFr
        record.phoneCode = dto.phoneCode
        record.emojiCode = emojiCode
    }

    private fun createEmojiCode(countryCode: String): String {
        val flagOffset = 0x1F1E6
        val asciiOffset = 0x41
        val firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset
        val secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset
        return (String(Character.toChars(firstChar))
                + String(Character.toChars(secondChar)))
    }
}