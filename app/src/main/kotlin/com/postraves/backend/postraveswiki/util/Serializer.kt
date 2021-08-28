package com.postraves.backend.postraveswiki.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object KOffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        val format = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        val string = format.format(value)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        val string = decoder.decodeString()
        return OffsetDateTime.parse(string)
    }
}

object KLocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        val format = DateTimeFormatter.ISO_LOCAL_DATE

        val string = format.format(value)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val string = decoder.decodeString()
        return LocalDate.parse(string)
    }
}
