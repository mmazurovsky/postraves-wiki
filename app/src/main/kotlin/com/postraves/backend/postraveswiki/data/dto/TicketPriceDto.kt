package com.postraves.backend.postraveswiki.data.dto

import com.postraves.backend.postraveswiki.data.enum.MoneyCurrency
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.TicketPriceRecord
import kotlinx.serialization.Serializable

@Serializable
data class TicketPriceDto (
    private val name: String,
    private val price: Double,
    private val currency: MoneyCurrency
) {
    companion object {
        fun createOutOfDbRecords(ticketPriceRecord: TicketPriceRecord): TicketPriceDto {
            return TicketPriceDto(
                name = ticketPriceRecord.name ?: throw RecordFieldNullException("Ticket price"),
                price = ticketPriceRecord.price ?: throw RecordFieldNullException("Ticket price"),
                currency = MoneyCurrency.valueOf(ticketPriceRecord.currency ?: throw RecordFieldNullException("Ticket money currency"))
            )
        }
    }

    fun transferDataToDbRecord(record: TicketPriceRecord) {
        record.name = name
        record.price = price
        record.currency = currency.name
    }
}
