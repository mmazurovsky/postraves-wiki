package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.reading.CountryDto
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.CountryRecord
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.util.*

interface CountryConverters {
    fun createDtoFromRecord(countryRecord: CountryRecord): CountryDto
    fun transferDataFromDtoToRecord(dto: CountryWriteDto, record: CountryRecord)
}

@Service
class CountryConvertersImpl(
) : CountryConverters {

    private fun resolveLocalizedName(countryRecord: CountryRecord): String? {
        val userLocale = LocaleContextHolder.getLocale()
        return if (userLocale.language.equals(Locale("ru").language)) {
            countryRecord.nameRu
        } else if (userLocale.language.equals(Locale("de").language)) {
            countryRecord.nameDe
        } else if (userLocale.language.equals(Locale("fr").language)) {
            countryRecord.nameFr
        } else {
            countryRecord.nameUk
        }
    }

    override fun createDtoFromRecord(countryRecord: CountryRecord): CountryDto {
        return CountryDto(
            name = countryRecord.name ?: throw RecordFieldNullException("Country Name"),
            localizedName = resolveLocalizedName(countryRecord) ?: throw RecordFieldNullException("Country Name"),
            phoneCode = countryRecord.phoneCode ?: throw RecordFieldNullException("Country phone code"),
            emojiCode = countryRecord.emojiCode ?: throw RecordFieldNullException("Country emoji code"),
        )
    }

    override fun transferDataFromDtoToRecord(dto: CountryWriteDto, record: CountryRecord) {
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