package com.sapuseven.untis.api.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object LocalDateSerializer : KSerializer<LocalDate> {
	private val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")

	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("java.time.LocalDate", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: LocalDate) {
		val string = value.format(format)
		encoder.encodeString(string)
	}

	override fun deserialize(decoder: Decoder): LocalDate {
		val string = decoder.decodeString()
		return LocalDate.parse(string, format)
	}
}
