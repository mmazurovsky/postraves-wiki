package com.postraves.backend.postraveswiki.data.converters

import com.postraves.backend.postraveswiki.data.dto.reading.TicketPriceDto
import com.postraves.backend.postraveswiki.data.dto.writing.TicketPriceWriteDto
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.MoneyCurrencyRecord
import jooq.tables.records.TicketPriceRecord
import org.springframework.stereotype.Service

interface TicketPriceConverters {
    fun createOutOfDbRecords(
        ticketPriceRecord: TicketPriceRecord,
        moneyCurrencyRecord: MoneyCurrencyRecord
    ): TicketPriceDto

    fun transferDataToDbRecord(dto: TicketPriceWriteDto, record: TicketPriceRecord)
}

@Service
class TicketPriceConvertersImpl(
    private val moneyCurrencyConverters: MoneyCurrencyConverters,
) : TicketPriceConverters {
    override fun createOutOfDbRecords(
        ticketPriceRecord: TicketPriceRecord,
        moneyCurrencyRecord: MoneyCurrencyRecord
    ): TicketPriceDto {
        return TicketPriceDto(
            name = ticketPriceRecord.ticketPriceName ?: throw RecordFieldNullException("Ticket price"),
            price = ticketPriceRecord.ticketPricePrice ?: throw RecordFieldNullException("Ticket price"),
            currency = moneyCurrencyConverters.createDtoFromRecord(moneyCurrencyRecord),
        )
    }


    override fun transferDataToDbRecord(dto: TicketPriceWriteDto, record: TicketPriceRecord) {
        record.ticketPriceName = dto.name
        record.ticketPricePrice = dto.price
        record.ticketPriceCurrency = dto.currency
    }
}