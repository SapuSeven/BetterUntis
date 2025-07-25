package com.sapuseven.untis.api.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object LocalTimeSerializer : KSerializer<LocalTime> {
	private val format = DateTimeFormatter.ofPattern("'T'HH:mm")

	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("java.time.LocalTime", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: LocalTime) {
		val string = value.format(format)
		encoder.encodeString(string)
	}

	override fun deserialize(decoder: Decoder): LocalTime {
		val string = decoder.decodeString()
		return try {
			LocalTime.parse(string, format)
		} catch (e: DateTimeParseException) {
			Instant.ofEpochMilli(0).atZone(ZoneId.systemDefault()).toLocalTime()
		}
	}
}
