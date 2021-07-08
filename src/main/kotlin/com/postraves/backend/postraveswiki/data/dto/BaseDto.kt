package com.postraves.backend.postraveswiki.data.dto

import org.jooq.UpdatableRecord

interface BaseDto

interface BaseShortDto : BaseDto

interface BaseFullDto : BaseDto

interface BaseWriteDto<RECORD : UpdatableRecord<RECORD>> : BaseDto {
    fun convertToDbRecord() : RECORD
}
