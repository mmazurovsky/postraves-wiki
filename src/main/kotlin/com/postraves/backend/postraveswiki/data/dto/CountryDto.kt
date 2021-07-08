package com.postraves.backend.postraveswiki.data.dto

import jooq.tables.records.CountryRecord

data class CountryDto(
    val name: String,
    val phoneCode: String,
    val emojiCode: String,
) : BaseDto, BaseWriteDto<CountryRecord> {
    override fun convertToDbRecord(): CountryRecord {
        TODO("Not yet implemented")
    }
}
