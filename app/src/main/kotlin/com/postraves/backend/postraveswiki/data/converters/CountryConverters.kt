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
        return when {
            userLocale.language.equals(Locale("ru").language) -> {
                countryRecord.countryNameRu
            }
            userLocale.language.equals(Locale("de").language) -> {
                countryRecord.countryNameDe
            }
            userLocale.language.equals(Locale("fr").language) -> {
                countryRecord.countryNameFr
            }
            else -> {
                countryRecord.countryNameEn
            }
        }
    }

    override fun createDtoFromRecord(countryRecord: CountryRecord): CountryDto {
        return CountryDto(
            name = countryRecord.countryName ?: throw RecordFieldNullException("Country Name"),
            localName = resolveLocalizedName(countryRecord) ?: throw RecordFieldNullException("Country Name"),
            phoneCode = countryRecord.countryPhoneCode ?: throw RecordFieldNullException("Country phone code"),
            emojiCode = countryRecord.countryEmojiCode ?: throw RecordFieldNullException("Country emoji code"),
        )
    }

    override fun transferDataFromDtoToRecord(dto: CountryWriteDto, record: CountryRecord) {
        val emojiCode = createEmojiCode(dto.name)
        record.countryName = dto.name
        record.countryNameRu = dto.nameRu
        record.countryNameEn = dto.nameEn
        record.countryNameDe = dto.nameDe
        record.countryNameFr = dto.nameFr
        record.countryPhoneCode = dto.phoneCode
        record.countryEmojiCode = emojiCode
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