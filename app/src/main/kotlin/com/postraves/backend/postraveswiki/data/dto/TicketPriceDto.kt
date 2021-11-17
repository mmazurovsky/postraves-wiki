package com.postraves.backend.postraveswiki.data.dto

import com.postraves.backend.postraveswiki.data.enum.MoneyCurrency
import com.postraves.backend.postraveswiki.exception.RecordFieldNullException
import jooq.tables.records.TicketPriceRecord
import kotlinx.serialization.Serializable

@Serializable
data class TicketPriceDto (
    private val name: String?,
    private val price: Double,
    private val currency: MoneyCurrency
) {
    companion object {
        fun createOutOfDbRecords(ticketPriceRecord: TicketPriceRecord): TicketPriceDto {
            return TicketPriceDto(
                name = ticketPriceRecord.ticketPriceName ?: throw RecordFieldNullException("Ticket price"),
                price = ticketPriceRecord.ticketPricePrice ?: throw RecordFieldNullException("Ticket price"),
                currency = MoneyCurrency.valueOf(ticketPriceRecord.ticketPriceCurrency ?: throw RecordFieldNullException("Ticket money currency"))
            )
        }
    }

    fun transferDataToDbRecord(record: TicketPriceRecord) {
        record.ticketPriceName = name
        record.ticketPricePrice = price
        record.ticketPriceCurrency = currency.name
    }
}
